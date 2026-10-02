package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
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
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.CompleteMaintenanceUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompleteMaintenanceUseCaseImpl implements CompleteMaintenanceUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final AuditPort auditPort;
    private final Clock clock;

    @Override
    @Transactional
    public Vehicle complete(AuthenticatedUser caller, Integer vehicleId, BigDecimal cost) {
        Authorization.require(caller, Permissions.MAINTENANCE_UPDATE);

        Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        VehicleMaintenance maintenance = vehicleMaintenanceRepositoryPort.findInProgressByVehicleId(vehicleId)
                .orElseThrow(() -> new BusinessRuleException("NO_MAINTENANCE_IN_PROGRESS",
                        "The vehicle has no maintenance in progress"));

        //It can be closed the same day it started: end_date is never before start_date
        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setEndDate(LocalDate.now(clock));
        maintenance.setCost(cost);
        vehicleMaintenanceRepositoryPort.save(maintenance);

        //One of the only 3 paths back to AVAILABLE (decisions.md)
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        Vehicle saved = vehicleRepositoryPort.save(vehicle);

        //HashMap because cost may be null (Map.of rejects nulls)
        Map<String, Object> detail = new HashMap<>();
        detail.put("vehicleId", vehicleId);
        detail.put("maintenanceId", maintenance.getId());
        detail.put("cost", cost);
        auditPort.record(caller.getUserId(), "MAINTENANCE_COMPLETED", "vehicle_maintenance", maintenance.getId(), detail);
        //booking-reservation consumes it to create the MAINTENANCE_COMPLETED notification
        eventPublisherPort.publish(new DomainEvent(DomainEvent.MAINTENANCE_COMPLETED, "Vehicle", vehicleId, detail));
        return saved;
    }
}
