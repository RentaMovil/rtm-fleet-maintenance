package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.port.in.ListInventoryUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListInventoryUseCaseImpl implements ListInventoryUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public List<Vehicle> list(AuthenticatedUser caller, VehicleStatus status, Integer branchId) {
        //Same rule as the stats panel: it belongs to whoever manages the fleet
        Authorization.require(caller, Permissions.VEHICLES_UPDATE);
        return vehicleRepositoryPort.findInventory(status, branchId);
    }
}
