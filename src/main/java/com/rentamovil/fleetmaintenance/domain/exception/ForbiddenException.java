package com.rentamovil.fleetmaintenance.domain.exception;

// The caller lacks the required permission (e.g. a CLIENT trying to register a vehicle). Maps to 403.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("You don't have permission to perform this action");
    }
}
