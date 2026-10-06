package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.CompleteMaintenanceUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompleteMaintenanceUseCaseImpl implements CompleteMaintenanceUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    private final MaintenanceLifecycle maintenanceLifecycle;

    @Override
    @Transactional
    public Vehicle complete(AuthenticatedUser caller, Integer vehicleId, BigDecimal cost) {
        Authorization.require(caller, Permissions.MAINTENANCE_UPDATE);

        Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        VehicleMaintenance maintenance = vehicleMaintenanceRepositoryPort.findInProgressByVehicleId(vehicleId)
                .orElseThrow(() -> new BusinessRuleException("NO_MAINTENANCE_IN_PROGRESS",
                        "The vehicle has no maintenance in progress"));

        //Same as PATCH /maintenances/{id}/status with COMPLETED (fleet-maintenance.yaml)
        maintenanceLifecycle.complete(caller.getUserId(), vehicle, maintenance, cost);
        return vehicle;
    }
}
