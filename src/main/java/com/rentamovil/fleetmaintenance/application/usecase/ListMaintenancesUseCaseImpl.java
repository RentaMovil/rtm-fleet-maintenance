package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.port.in.ListMaintenancesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListMaintenancesUseCaseImpl implements ListMaintenancesUseCase {

    private final VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;

    @Override
    public List<VehicleMaintenance> list(AuthenticatedUser caller, Integer vehicleId, MaintenanceStatus status) {
        //There is no "maintenance:read" permission: the history belongs to whoever manages maintenances
        Authorization.require(caller, Permissions.MAINTENANCE_UPDATE);
        return vehicleMaintenanceRepositoryPort.search(vehicleId, status);
    }
}
