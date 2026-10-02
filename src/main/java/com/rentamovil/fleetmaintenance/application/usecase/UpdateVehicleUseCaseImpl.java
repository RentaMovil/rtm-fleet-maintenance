package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.UpdateVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.VehicleData;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateVehicleUseCaseImpl implements UpdateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final VehicleDataResolver vehicleDataResolver;
    private final AuditPort auditPort;

    @Override
    @Transactional
    public Vehicle update(AuthenticatedUser caller, Integer vehicleId, VehicleData data) {
        Authorization.require(caller, Permissions.VEHICLES_UPDATE);

        Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        //RETIRED is terminal: it never returns to the catalog, so editing it makes no sense
        if (vehicle.isRetired()) {
            throw new BusinessRuleException("VEHICLE_RETIRED", "The vehicle is retired and can't be edited");
        }

        String plate = VehicleDataResolver.normalizePlate(data.plate());
        if (!plate.equals(vehicle.getPlate()) && vehicleRepositoryPort.existsByPlate(plate)) {
            throw new ResourceAlreadyExistsException("PLATE_ALREADY_EXISTS", "The plate already exists");
        }

        //Mileage never decreases (Vehicle.INV-004). If missing, the current value is kept: with the contract's
        //default of 0, a PUT without mileage would always fail.
        BigDecimal mileage = data.mileage() == null ? vehicle.getMileage() : data.mileage();
        if (mileage.compareTo(vehicle.getMileage()) < 0) {
            throw new BusinessRuleException("MILEAGE_CANNOT_DECREASE",
                    "Mileage can't be lower than the current one (" + vehicle.getMileage() + ")");
        }

        //Full replacement, but the status is untouched: it has its own paths (decisions.md)
        vehicle.setPlate(plate);
        vehicle.setModel(vehicleDataResolver.resolveModel(data));
        vehicle.setBranch(vehicleDataResolver.resolveBranch(data));
        vehicle.setCapacity(data.capacity());
        vehicle.setYear(data.year());
        vehicle.setDailyPrice(data.dailyPrice());
        vehicle.setMileage(mileage);
        vehicle.setImageUrl(data.imageUrl());

        Vehicle saved = vehicleRepositoryPort.save(vehicle);
        auditPort.record(caller.getUserId(), "VEHICLE_UPDATED", "vehicle", saved.getId(),
                Map.of("plate", saved.getPlate(), "dailyPrice", saved.getDailyPrice()));
        return saved;
    }
}
