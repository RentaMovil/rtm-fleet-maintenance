package com.rentamovil.fleetmaintenance.application.usecase;

import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.common.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.CheckVehicleAvailabilityUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckVehicleAvailabilityUseCaseImpl implements CheckVehicleAvailabilityUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final ReservationOccupancyPort reservationOccupancyPort;

    @Override
    public boolean isAvailable(Integer vehicleId, LocalDate from, LocalDate to) {
        DateRanges.validate(from, to, "from", "to");

        Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        //Available = AVAILABLE and no overlapping reservations (fleet-maintenance.yaml). A vehicle in
        //MAINTENANCE can't be reserved (Vehicle.INV-002): booking-reservation isn't even asked.
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            return false;
        }
        return !reservationOccupancyPort.isOccupied(vehicleId, from, to);
    }
}
