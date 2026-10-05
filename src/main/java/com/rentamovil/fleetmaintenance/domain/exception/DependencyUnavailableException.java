package com.rentamovil.fleetmaintenance.domain.exception;

import lombok.Getter;

// Another service the operation depends on is not responding (booking-reservation for occupancy,
// iam for the token keys). Not the caller's fault: maps to 503.
@Getter
public class DependencyUnavailableException extends RuntimeException {

    // BOOKING_UNAVAILABLE o IAM_UNAVAILABLE
    private final String code;

    public DependencyUnavailableException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
