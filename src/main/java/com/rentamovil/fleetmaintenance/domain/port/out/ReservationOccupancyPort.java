package com.rentamovil.fleetmaintenance.domain.port.out;

import java.time.LocalDate;

// Asks booking-reservation (the owner of reservations) whether a vehicle has a non-cancelled
// reservation overlapping [from, to). This service never reads the reservation table.
// Throws DependencyUnavailableException if booking-reservation doesn't respond.
public interface ReservationOccupancyPort {

    boolean isOccupied(Integer vehicleId, LocalDate from, LocalDate to);
}
