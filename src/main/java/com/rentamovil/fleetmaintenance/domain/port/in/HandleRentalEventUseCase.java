package com.rentamovil.fleetmaintenance.domain.port.in;

// Reacts to booking-reservation events: RentalStarted -> RENTED, RentalCompleted -> AVAILABLE.
// It's the only way another service changes vehicle.status. Idempotent: the same event
// twice leaves the same status (delivery is at-least-once).
public interface HandleRentalEventUseCase {

    void rentalStarted(Integer vehicleId);

    void rentalCompleted(Integer vehicleId);
}
