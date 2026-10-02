package com.rentamovil.fleetmaintenance.domain.exception;

import lombok.Getter;

// A business rule forbids the operation (e.g. sending a RENTED vehicle to maintenance). Maps to 409.
@Getter
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }
}
