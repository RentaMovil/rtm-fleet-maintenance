package com.rentamovil.fleetmaintenance.domain.exception;

import lombok.Getter;

// A request value is not valid for the business (e.g. a modelId that doesn't exist, or a branch
// that opens after it closes). Maps to 400 VALIDATION_ERROR naming the field.
@Getter
public class InvalidDataException extends RuntimeException {

    private final String field;

    public InvalidDataException(String field, String message) {
        super(message);
        this.field = field;
    }
}
