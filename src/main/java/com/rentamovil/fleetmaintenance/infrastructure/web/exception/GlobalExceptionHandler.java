package com.rentamovil.fleetmaintenance.infrastructure.web.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rentamovil.common.web.dto.ErrorResponse;
import com.rentamovil.common.web.exception.BaseExceptionHandler;
import com.rentamovil.fleetmaintenance.domain.exception.DependencyUnavailableException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;

import lombok.extern.slf4j.Slf4j;

// Turns exceptions into responses with the ErrorResponse format of _shared.yaml.
// Generic cases (validation, bad JSON, the common domain exceptions, Spring's own web errors,
// the unhandled-exception fallback) are handled by BaseExceptionHandler (rtm-common, ADR-011).
// Only this service's own exceptions are handled here.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidData(InvalidDataException e) {
        return new ErrorResponse("VALIDATION_ERROR", "Invalid input data",
                List.of(new ErrorResponse.FieldError(e.getField(), e.getMessage())));
    }

    // booking-reservation or iam not responding: answer 503, technical detail only goes to the log
    @ExceptionHandler(DependencyUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleDependencyUnavailable(DependencyUnavailableException e) {
        log.warn("{}: {}", e.getCode(), e.getMessage(), e.getCause());
        return new ErrorResponse(e.getCode(), e.getMessage());
    }

    // What the database stops even though the use case already checked it: two registrations with
    // the same plate at once, or deleting a branch a reservation still uses
    @Override
    protected ErrorResponse mapIntegrityViolation(String cause) {
        if (cause.contains("uq_vehicle_plate")) {
            return new ErrorResponse("PLATE_ALREADY_EXISTS", "The plate already exists");
        }
        if (cause.contains("REFERENCE constraint")) {
            return new ErrorResponse("RESOURCE_IN_USE", "Another record still depends on this one (e.g. a reservation)");
        }
        return null;
    }
}
