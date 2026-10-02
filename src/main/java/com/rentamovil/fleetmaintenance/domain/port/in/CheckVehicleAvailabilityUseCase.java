package com.rentamovil.fleetmaintenance.domain.port.in;

import java.time.LocalDate;

// Can the vehicle be reserved for [from, to)? (HU-FLEET-002). Public; booking-reservation uses it too.
public interface CheckVehicleAvailabilityUseCase {

    boolean isAvailable(Integer vehicleId, LocalDate from, LocalDate to);
}
