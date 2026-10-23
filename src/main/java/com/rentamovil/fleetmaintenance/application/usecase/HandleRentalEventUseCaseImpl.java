package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.common.exception.BusinessRuleException;
import com.rentamovil.common.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.HandleRentalEventUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

// Pickup and return live in booking-reservation, but vehicle belongs to this service: the status
// change arrives as an event, never as a direct write to the table (service-catalog.md)
@RequiredArgsConstructor
public class HandleRentalEventUseCaseImpl implements HandleRentalEventUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final AuditPort auditPort;

    @Override
    @Transactional
    public void rentalStarted(Integer vehicleId) {
        move(vehicleId, VehicleStatus.AVAILABLE, VehicleStatus.RENTED, "VEHICLE_RENTED");
    }

    @Override
    @Transactional
    public void rentalCompleted(Integer vehicleId) {
        move(vehicleId, VehicleStatus.RENTED, VehicleStatus.AVAILABLE, "VEHICLE_RETURNED");
    }

    private void move(Integer vehicleId, VehicleStatus from, VehicleStatus to, String auditAction) {
        Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        //The same event may arrive twice (at-least-once): if it's already in the final status, do nothing
        if (vehicle.getStatus() == to) {
            return;
        }
        //E.g. RentalStarted for a vehicle in MAINTENANCE: the two services' data disagree.
        //It's rejected so booking-reservation notices, instead of silently overwriting the status.
        if (vehicle.getStatus() != from) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "The vehicle is " + vehicle.getStatus() + " and can't move to " + to);
        }

        vehicle.setStatus(to);
        vehicleRepositoryPort.save(vehicle);
        //No user: the system did it, reacting to an event
        auditPort.record(null, auditAction, "vehicle", vehicleId,
                Map.of("oldStatus", from.name(), "newStatus", to.name()));
    }
}
