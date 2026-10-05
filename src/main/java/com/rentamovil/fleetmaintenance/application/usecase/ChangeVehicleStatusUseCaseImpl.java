package com.rentamovil.fleetmaintenance.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.DomainEvent;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.ChangeVehicleStatusUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;
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
    private final EventPublisherPort eventPublisherPort;
    private final AuditPort auditPort;
    private final Clock clock;
    // How many days ahead to look for reservations before retiring a vehicle
    private final long retireReservationHorizonDays;

    // Mandar a mantenimiento escribe 2 tablas (vehicle_maintenance y vehicle): van juntas o ninguna
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

        VehicleMaintenance maintenance = vehicleMaintenanceRepositoryPort.save(VehicleMaintenance.builder()
                .vehicleId(vehicle.getId())
                .maintenanceTypeId(maintenanceTypeId)
                .startDate(LocalDate.now(clock))
                .status(MaintenanceStatus.IN_PROGRESS)
                .build());

        //While a maintenance is IN_PROGRESS the vehicle must be in MAINTENANCE (VehicleMaintenance.INV-001)
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        Vehicle saved = vehicleRepositoryPort.save(vehicle);

        Map<String, Object> detail = Map.of(
                "vehicleId", vehicle.getId(),
                "maintenanceId", maintenance.getId(),
                "maintenanceTypeId", maintenanceTypeId);
        auditPort.record(caller.getUserId(), "VEHICLE_SENT_TO_MAINTENANCE", "vehicle", vehicle.getId(), detail);
        eventPublisherPort.publish(new DomainEvent(DomainEvent.VEHICLE_SENT_TO_MAINTENANCE, "Vehicle",
                vehicle.getId(), detail));
        return saved;
    }

    // HU-FLEET-005 scenarios 2-3: from AVAILABLE or MAINTENANCE and with no future reservations (Vehicle.INV-006)
    private Vehicle retire(AuthenticatedUser caller, Vehicle vehicle) {
        LocalDate today = LocalDate.now(clock);
        if (reservationOccupancyPort.isOccupied(vehicle.getId(), today, today.plusDays(retireReservationHorizonDays))) {
            throw new BusinessRuleException("VEHICLE_HAS_FUTURE_RESERVATIONS",
                    "The vehicle has non-cancelled future reservations");
        }

        VehicleStatus oldStatus = vehicle.getStatus();

        //If it was under maintenance, that record is closed: an IN_PROGRESS maintenance requires the
        //vehicle to stay in MAINTENANCE (VehicleMaintenance.INV-001). MaintenanceCompleted is not emitted:
        //the vehicle never becomes available again, so there is nothing to notify.
        Integer closedMaintenanceId = null;
        if (oldStatus == VehicleStatus.MAINTENANCE) {
            closedMaintenanceId = vehicleMaintenanceRepositoryPort.findInProgressByVehicleId(vehicle.getId())
                    .map(maintenance -> {
                        maintenance.setStatus(MaintenanceStatus.COMPLETED);
                        maintenance.setEndDate(today);
                        return vehicleMaintenanceRepositoryPort.save(maintenance).getId();
                    })
                    .orElse(null);
        }

        vehicle.setStatus(VehicleStatus.RETIRED);
        Vehicle saved = vehicleRepositoryPort.save(vehicle);

        Map<String, Object> detail = closedMaintenanceId == null
                ? Map.of("oldStatus", oldStatus.name())
                : Map.of("oldStatus", oldStatus.name(), "closedMaintenanceId", closedMaintenanceId);
        auditPort.record(caller.getUserId(), "VEHICLE_RETIRED", "vehicle", vehicle.getId(), detail);
        return saved;
    }
}
