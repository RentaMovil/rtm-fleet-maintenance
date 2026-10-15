package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.rentamovil.fleetmaintenance.domain.entity.DomainEvent;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

// The transitions of a maintenance that also move the vehicle (VehicleMaintenance lifecycle).
// Shared by the vehicle endpoints (send to maintenance, complete) and /maintenances, so both paths
// apply the same rules, audit rows and events. Callers check permissions and preconditions first.
@RequiredArgsConstructor
public class MaintenanceLifecycle {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final AuditPort auditPort;
    private final Clock clock;

    LocalDate today() {
        return LocalDate.now(clock);
    }

    // → IN_PROGRESS. The vehicle must be AVAILABLE (checked by the caller) and moves to MAINTENANCE
    // (VehicleMaintenance.INV-001)
    VehicleMaintenance start(Integer actorUserId, Vehicle vehicle, VehicleMaintenance maintenance) {
        maintenance.setStatus(MaintenanceStatus.IN_PROGRESS);
        VehicleMaintenance saved = vehicleMaintenanceRepositoryPort.save(maintenance);

        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        vehicleRepositoryPort.save(vehicle);

        Map<String, Object> detail = Map.of(
                "vehicleId", vehicle.getId(),
                "maintenanceId", saved.getId(),
                "maintenanceTypeId", saved.getMaintenanceTypeId());
        auditPort.record(actorUserId, "VEHICLE_SENT_TO_MAINTENANCE", "vehicle", vehicle.getId(), detail);
        eventPublisherPort.publish(new DomainEvent(DomainEvent.VEHICLE_SENT_TO_MAINTENANCE, "Vehicle",
                vehicle.getId(), detail));
        return saved;
    }

    // IN_PROGRESS → COMPLETED: one of the only 3 paths back to AVAILABLE (decisions.md)
    VehicleMaintenance complete(Integer actorUserId, Vehicle vehicle, VehicleMaintenance maintenance, BigDecimal cost) {
        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setEndDate(today());
        if (cost != null) {
            maintenance.setCost(cost);
        }
        VehicleMaintenance saved = vehicleMaintenanceRepositoryPort.save(maintenance);

        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepositoryPort.save(vehicle);

        //HashMap because cost may be null (Map.of rejects nulls)
        Map<String, Object> detail = new HashMap<>();
        detail.put("vehicleId", vehicle.getId());
        detail.put("maintenanceId", saved.getId());
        detail.put("cost", saved.getCost());
        auditPort.record(actorUserId, "MAINTENANCE_COMPLETED", "vehicle_maintenance", saved.getId(), detail);
        //booking-reservation consumes it to create the MAINTENANCE_COMPLETED notification
        eventPublisherPort.publish(new DomainEvent(DomainEvent.MAINTENANCE_COMPLETED, "Vehicle", vehicle.getId(), detail));
        return saved;
    }

    // SCHEDULED/IN_PROGRESS → CANCELLED: replaces deleting (INV-005). No event (events.md).
    // releaseVehicle = false when the vehicle is being retired: it doesn't go back to AVAILABLE.
    VehicleMaintenance cancel(Integer actorUserId, Vehicle vehicle, VehicleMaintenance maintenance, boolean releaseVehicle) {
        boolean wasInProgress = maintenance.getStatus() == MaintenanceStatus.IN_PROGRESS;
        maintenance.setStatus(MaintenanceStatus.CANCELLED);
        maintenance.setEndDate(today());
        VehicleMaintenance saved = vehicleMaintenanceRepositoryPort.save(maintenance);

        if (wasInProgress && releaseVehicle) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepositoryPort.save(vehicle);
        }

        auditPort.record(actorUserId, "MAINTENANCE_CANCELLED", "vehicle_maintenance", saved.getId(),
                Map.of("vehicleId", vehicle.getId(), "wasInProgress", wasInProgress));
        return saved;
    }
}
