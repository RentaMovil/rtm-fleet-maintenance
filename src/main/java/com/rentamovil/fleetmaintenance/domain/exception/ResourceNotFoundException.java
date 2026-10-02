package com.rentamovil.fleetmaintenance.domain.exception;

// What the URL points to doesn't exist (e.g. GET /vehicles/99). Maps to 404.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
