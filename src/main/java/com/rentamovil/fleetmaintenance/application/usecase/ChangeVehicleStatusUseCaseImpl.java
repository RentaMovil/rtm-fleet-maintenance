package com.rentamovil.fleetmaintenance.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.ChangeVehicleStatusUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeVehicleStatusUseCaseImpl implements ChangeVehicleStatusUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    private final ReferenceDataRepositoryPort referenceDataRepositoryPort;
    private final ReservationOccupancyPort reservationOccupancyPort;
    private final MaintenanceLifecycle maintenanceLifecycle;
    private final AuditPort auditPort;
    // How many days ahead to look for reservations before retiring a vehicle
    private final long retireReservationHorizonDays;

    // Sending to maintenance writes 2 tables (vehicle_maintenance and vehicle): together or not at all
    @Override
    @Transactional
    public Vehicle changeStatus(AuthenticatedUser caller, Integer vehicleId, VehicleStatus newStatus,
                                Integer maintenanceTypeId) {

        //AVAILABLE is never a manual target: it's reached on creation, on return, or by closing a maintenance
        switch (newStatus) {
            case MAINTENANCE -> Authorization.require(caller, Permissions.MAINTENANCE_CREATE);
            case RETIRED -> Authorization.require(caller, Permissions.VEHICLES_UPDATE);
            default -> throw new InvalidDataException("status", "Only MAINTENANCE or RETIRED are accepted");
        }

        Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        VehicleStatus oldStatus = vehicle.getStatus();

        //A rented vehicle must first go back to AVAILABLE through its return (Vehicle.INV-005)
        if (oldStatus == VehicleStatus.RENTED) {
            throw new BusinessRuleException("VEHICLE_RENTED",
                    "The vehicle is rented: its return must be registered first");
        }
        if (oldStatus == VehicleStatus.RETIRED) {
            throw new BusinessRuleException("VEHICLE_RETIRED", "The vehicle is already retired");
        }

        return newStatus == VehicleStatus.MAINTENANCE
                ? sendToMaintenance(caller, vehicle, maintenanceTypeId)
                : retire(caller, vehicle);
    }

    // HU-FLEET-006 scenario 1: only from AVAILABLE
    private Vehicle sendToMaintenance(AuthenticatedUser caller, Vehicle vehicle, Integer maintenanceTypeId) {
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION", "The vehicle is already under maintenance");
        }
        if (maintenanceTypeId == null) {
            throw new InvalidDataException("maintenanceTypeId", "Required when status = MAINTENANCE");
        }
        if (!referenceDataRepositoryPort.existsMaintenanceType(maintenanceTypeId)) {
            throw new InvalidDataException("maintenanceTypeId", "Maintenance type not found: " + maintenanceTypeId);
        }

        //Same as POST /maintenances with status = IN_PROGRESS (fleet-maintenance.yaml)
        maintenanceLifecycle.start(caller.getUserId(), vehicle, VehicleMaintenance.builder()
                .vehicleId(vehicle.getId())
                .maintenanceTypeId(maintenanceTypeId)
                .startDate(maintenanceLifecycle.today())
                .build());
        return vehicle;
    }

    // HU-FLEET-005 scenarios 2-3: from AVAILABLE or MAINTENANCE and with no future reservations (Vehicle.INV-006)
    private Vehicle retire(AuthenticatedUser caller, Vehicle vehicle) {
        LocalDate today = maintenanceLifecycle.today();
        if (reservationOccupancyPort.isOccupied(vehicle.getId(), today, today.plusDays(retireReservationHorizonDays))) {
            throw new BusinessRuleException("VEHICLE_HAS_FUTURE_RESERVATIONS",
                    "The vehicle has non-cancelled future reservations");
        }

        VehicleStatus oldStatus = vehicle.getStatus();

        //Its open maintenances (SCHEDULED or IN_PROGRESS) are cancelled (VehicleMaintenance.INV-007).
        //The vehicle doesn't go back to AVAILABLE and no event is emitted: it leaves the fleet.
        List<Integer> cancelledMaintenanceIds = vehicleMaintenanceRepositoryPort.findOpenByVehicleId(vehicle.getId()).stream()
                .map(maintenance -> maintenanceLifecycle.cancel(caller.getUserId(), vehicle, maintenance, false).getId())
                .toList();

        vehicle.setStatus(VehicleStatus.RETIRED);
        Vehicle saved = vehicleRepositoryPort.save(vehicle);

        Map<String, Object> detail = cancelledMaintenanceIds.isEmpty()
                ? Map.of("oldStatus", oldStatus.name())
                : Map.of("oldStatus", oldStatus.name(), "cancelledMaintenanceIds", cancelledMaintenanceIds);
        auditPort.record(caller.getUserId(), "VEHICLE_RETIRED", "vehicle", vehicle.getId(), detail);
        return saved;
    }
}
