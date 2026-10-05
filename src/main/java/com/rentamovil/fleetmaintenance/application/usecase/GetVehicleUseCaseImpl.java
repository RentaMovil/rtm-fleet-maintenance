package com.rentamovil.fleetmaintenance.application.usecase;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.GetVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetVehicleUseCaseImpl implements GetVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public Vehicle getVehicle(Integer vehicleId) {
        return vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
    }
}
