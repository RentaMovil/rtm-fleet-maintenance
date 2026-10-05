package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.MaintenanceRecord;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.port.in.ListMaintenancesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListMaintenancesUseCaseImpl implements ListMaintenancesUseCase {

    private final VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;

    @Override
    public List<MaintenanceRecord> list(AuthenticatedUser caller, Integer vehicleId, MaintenanceStatus status) {
        //Whoever registers or closes maintenances can read their history; a CLIENT can't
        Authorization.require(caller, Permissions.MAINTENANCE_UPDATE);
        return vehicleMaintenanceRepositoryPort.findHistory(vehicleId, status);
    }
}
