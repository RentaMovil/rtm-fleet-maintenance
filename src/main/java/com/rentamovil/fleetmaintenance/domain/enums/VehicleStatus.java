package com.rentamovil.fleetmaintenance.domain.enums;

// Vehicle lifecycle (02-domain/entities-and-rules.md -> Vehicle). There is no RESERVED:
// whether a vehicle is free for some dates is computed from reservations, not from this status.
public enum VehicleStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE,
    // Logical retirement, terminal. The row is never deleted: past reservations and rentals reference it.
    RETIRED
}
