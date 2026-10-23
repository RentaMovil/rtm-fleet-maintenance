package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.common.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.common.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.MaintenanceData;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageMaintenanceUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

// HU-FLEET-008. Rules: 02-domain/entities-and-rules.md -> VehicleMaintenance (INV-001 to INV-007)
@RequiredArgsConstructor
public class ManageMaintenanceUseCaseImpl implements ManageMaintenanceUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    private final ReferenceDataRepositoryPort referenceDataRepositoryPort;
    private final MaintenanceLifecycle maintenanceLifecycle;
    private final AuditPort auditPort;

    // Maintenance + vehicle (+ audit) are saved together or not at all
    @Override
    @Transactional
    public VehicleMaintenance create(AuthenticatedUser caller, Integer vehicleId, MaintenanceStatus status,
                                     MaintenanceData data) {
        Authorization.require(caller, Permissions.MAINTENANCE_CREATE);
        if (status != MaintenanceStatus.SCHEDULED && status != MaintenanceStatus.IN_PROGRESS) {
            throw new InvalidDataException("status", "Only SCHEDULED or IN_PROGRESS are accepted");
        }

        Vehicle vehicle = findVehicle(vehicleId);
        //No maintenance for a retired vehicle (INV-006)
        if (vehicle.isRetired()) {
            throw new BusinessRuleException("VEHICLE_RETIRED", "The vehicle is retired");
        }
        requireMaintenanceType(data.maintenanceTypeId());

        LocalDate today = maintenanceLifecycle.today();
        VehicleMaintenance maintenance = VehicleMaintenance.builder()
                .vehicleId(vehicleId)
                .maintenanceTypeId(data.maintenanceTypeId())
                .cost(data.cost())
                .observations(blankToNull(data.observations()))
                .imageUrl(blankToNull(data.imageUrl()))
                .build();

        if (status == MaintenanceStatus.SCHEDULED) {
            //A scheduled maintenance doesn't touch the vehicle: it can still be rented until it starts
            if (data.startDate() == null) {
                throw new InvalidDataException("startDate", "Required when status = SCHEDULED");
            }
            validateStartDate(MaintenanceStatus.SCHEDULED, data.startDate(), today);
            maintenance.setStartDate(data.startDate());
            maintenance.setStatus(MaintenanceStatus.SCHEDULED);
            VehicleMaintenance saved = vehicleMaintenanceRepositoryPort.save(maintenance);
            auditPort.record(caller.getUserId(), "MAINTENANCE_SCHEDULED", "vehicle_maintenance", saved.getId(),
                    Map.of("vehicleId", vehicleId, "startDate", saved.getStartDate().toString()));
            return reload(saved.getId());
        }

        //Starts now: only an AVAILABLE vehicle (INV-006); one already in MAINTENANCE has its own in progress (INV-004)
        requireAvailable(vehicle);
        LocalDate startDate = data.startDate() == null ? today : data.startDate();
        validateStartDate(MaintenanceStatus.IN_PROGRESS, startDate, today);
        maintenance.setStartDate(startDate);
        return reload(maintenanceLifecycle.start(caller.getUserId(), vehicle, maintenance).getId());
    }

    @Override
    @Transactional
    public VehicleMaintenance update(AuthenticatedUser caller, Integer maintenanceId, MaintenanceData data) {
        Authorization.require(caller, Permissions.MAINTENANCE_UPDATE);
        VehicleMaintenance maintenance = findMaintenance(maintenanceId);

        //Completed and cancelled are read-only: the history and the cost reports don't change afterwards (INV-003)
        if (maintenance.isClosed()) {
            throw new BusinessRuleException("MAINTENANCE_CLOSED", "The maintenance is " + maintenance.getStatus()
                    + " and can't be edited");
        }

        if (data.maintenanceTypeId() != null) {
            requireMaintenanceType(data.maintenanceTypeId());
            maintenance.setMaintenanceTypeId(data.maintenanceTypeId());
        }
        if (data.startDate() != null) {
            validateStartDate(maintenance.getStatus(), data.startDate(), maintenanceLifecycle.today());
            maintenance.setStartDate(data.startDate());
        }
        if (data.cost() != null) {
            maintenance.setCost(data.cost());
        }
        //An empty string clears the field; null leaves it as it was
        if (data.observations() != null) {
            maintenance.setObservations(blankToNull(data.observations()));
        }
        if (data.imageUrl() != null) {
            maintenance.setImageUrl(blankToNull(data.imageUrl()));
        }

        VehicleMaintenance saved = vehicleMaintenanceRepositoryPort.save(maintenance);
        auditPort.record(caller.getUserId(), "MAINTENANCE_UPDATED", "vehicle_maintenance", saved.getId(),
                Map.of("vehicleId", saved.getVehicleId()));
        return reload(saved.getId());
    }

    @Override
    @Transactional
    public VehicleMaintenance changeStatus(AuthenticatedUser caller, Integer maintenanceId,
                                           MaintenanceStatus newStatus, BigDecimal cost) {
        Authorization.require(caller, Permissions.MAINTENANCE_UPDATE);
        VehicleMaintenance maintenance = findMaintenance(maintenanceId);
        MaintenanceStatus current = maintenance.getStatus();
        Vehicle vehicle = findVehicle(maintenance.getVehicleId());

        if (current == MaintenanceStatus.SCHEDULED && newStatus == MaintenanceStatus.IN_PROGRESS) {
            requireAvailable(vehicle);
            //start_date becomes the real start
            maintenance.setStartDate(maintenanceLifecycle.today());
            maintenanceLifecycle.start(caller.getUserId(), vehicle, maintenance);
        } else if (current == MaintenanceStatus.IN_PROGRESS && newStatus == MaintenanceStatus.COMPLETED) {
            maintenanceLifecycle.complete(caller.getUserId(), vehicle, maintenance, cost);
        } else if (!current.isClosed() && newStatus == MaintenanceStatus.CANCELLED) {
            maintenanceLifecycle.cancel(caller.getUserId(), vehicle, maintenance, true);
        } else {
            //Includes anything out of COMPLETED/CANCELLED: they are terminal
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "A maintenance can't go from " + current + " to " + newStatus);
        }
        return reload(maintenanceId);
    }

    // SCHEDULED: today or later. IN_PROGRESS: it already started, so never in the future.
    private static void validateStartDate(MaintenanceStatus status, LocalDate startDate, LocalDate today) {
        if (status == MaintenanceStatus.SCHEDULED && startDate.isBefore(today)) {
            throw new InvalidDataException("startDate", "A scheduled maintenance can't be in the past");
        }
        if (status == MaintenanceStatus.IN_PROGRESS && startDate.isAfter(today)) {
            throw new InvalidDataException("startDate", "A maintenance in progress can't start in the future");
        }
    }

    private void requireAvailable(Vehicle vehicle) {
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessRuleException("VEHICLE_NOT_AVAILABLE",
                    "The vehicle is " + vehicle.getStatus() + ": a maintenance can only start on an AVAILABLE vehicle");
        }
    }

    private void requireMaintenanceType(Integer maintenanceTypeId) {
        if (maintenanceTypeId == null || !referenceDataRepositoryPort.existsMaintenanceType(maintenanceTypeId)) {
            throw new InvalidDataException("maintenanceTypeId", "Maintenance type not found: " + maintenanceTypeId);
        }
    }

    private Vehicle findVehicle(Integer vehicleId) {
        return vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
    }

    private VehicleMaintenance findMaintenance(Integer maintenanceId) {
        return vehicleMaintenanceRepositoryPort.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found: " + maintenanceId));
    }

    // Read it back with the vehicle and the type resolved, for the response
    private VehicleMaintenance reload(Integer maintenanceId) {
        return vehicleMaintenanceRepositoryPort.findById(maintenanceId).orElseThrow();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
