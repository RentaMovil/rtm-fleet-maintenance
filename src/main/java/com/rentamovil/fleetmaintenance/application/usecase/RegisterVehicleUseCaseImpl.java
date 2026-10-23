package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.common.exception.ResourceAlreadyExistsException;
import com.rentamovil.fleetmaintenance.domain.port.in.RegisterVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.VehicleData;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterVehicleUseCaseImpl implements RegisterVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final VehicleDataResolver vehicleDataResolver;
    private final AuditPort auditPort;

    // The vehicle and its audit row are saved together or not at all
    @Override
    @Transactional
    public Vehicle register(AuthenticatedUser caller, VehicleData data) {
        Authorization.require(caller, Permissions.VEHICLES_CREATE);

        String plate = VehicleDataResolver.normalizePlate(data.plate());
        if (vehicleRepositoryPort.existsByPlate(plate)) {
            throw new ResourceAlreadyExistsException("PLATE_ALREADY_EXISTS", "The plate already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .plate(plate)
                .model(vehicleDataResolver.resolveModel(data))
                .branch(vehicleDataResolver.resolveBranch(data))
                .capacity(data.capacity())
                .year(data.year())
                .dailyPrice(data.dailyPrice())
                .mileage(VehicleDataResolver.mileageOrZero(data.mileage()))
                .imageUrl(data.imageUrl())
                //Always created AVAILABLE: the status is not a form field (HU-FLEET-004)
                .status(VehicleStatus.AVAILABLE)
                .build();

        Vehicle saved = vehicleRepositoryPort.save(vehicle);
        auditPort.record(caller.getUserId(), "VEHICLE_REGISTERED", "vehicle", saved.getId(),
                Map.of("plate", saved.getPlate()));
        return saved;
    }
}
