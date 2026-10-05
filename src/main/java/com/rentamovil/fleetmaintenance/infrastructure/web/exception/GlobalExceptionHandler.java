package com.rentamovil.fleetmaintenance.infrastructure.web.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.DependencyUnavailableException;
import com.rentamovil.fleetmaintenance.domain.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidTokenException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

// Turns exceptions into responses with the ErrorResponse format of _shared.yaml
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> details = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return new ErrorResponse("VALIDATION_ERROR", "Invalid input data", details);
    }

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidData(InvalidDataException e) {
        return new ErrorResponse("VALIDATION_ERROR", "Invalid input data",
                List.of(new ErrorResponse.FieldError(e.getField(), e.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadJson() {
        return new ErrorResponse("VALIDATION_ERROR", "The request body is not valid JSON");
    }

    // E.g. /vehicles/abc or dateFrom=tomorrow: the value can't be converted to the expected type
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return new ErrorResponse("VALIDATION_ERROR", "Invalid input data",
                List.of(new ErrorResponse.FieldError(e.getName(), "Invalid format")));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExists(ResourceAlreadyExistsException e) {
        return new ErrorResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleBusinessRule(BusinessRuleException e) {
        return new ErrorResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException e) {
        return new ErrorResponse("FORBIDDEN", e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException e) {
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidToken(InvalidTokenException e) {
        return new ErrorResponse("INVALID_TOKEN", e.getMessage());
    }

    // booking-reservation or iam not responding: answer 503, technical detail only goes to the log
    @ExceptionHandler(DependencyUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleDependencyUnavailable(DependencyUnavailableException e) {
        log.warn("{}: {}", e.getCode(), e.getMessage(), e.getCause());
        return new ErrorResponse(e.getCode(), e.getMessage());
    }

    // What the database stops even though the use case already checked it: two registrations with the
    // same plate at once, or deleting a branch a reservation still uses
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIntegrity(DataIntegrityViolationException e) {
        String cause = String.valueOf(e.getMostSpecificCause().getMessage());
        if (cause.contains("uq_vehicle_plate")) {
            return new ErrorResponse("PLATE_ALREADY_EXISTS", "The plate already exists");
        }
        if (cause.contains("REFERENCE constraint")) {
            return new ErrorResponse("RESOURCE_IN_USE", "Another record still depends on this one (e.g. a reservation)");
        }
        log.error("Unexpected integrity violation", e);
        return new ErrorResponse("CONFLICT", "The data conflicts with an existing record");
    }

    // Spring's own errors that already carry an HTTP status: missing parameter (400), method not
    // allowed (405), unknown path (404), unsupported Content-Type (415)... That status is kept.
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleSpringWebError(ErrorResponseException e) {
        return springWebError(e.getStatusCode());
    }

    // The detail only goes to the log, never into the response
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        //MissingServletRequestParameterException and similar implement Spring's ErrorResponse interface
        if (e instanceof org.springframework.web.ErrorResponse springError) {
            return springWebError(springError.getStatusCode());
        }
        log.error("Unhandled error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private ResponseEntity<ErrorResponse> springWebError(HttpStatusCode status) {
        String code = switch (status.value()) {
            case 400 -> "VALIDATION_ERROR";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> "REQUEST_ERROR";
        };
        String message = status.value() == 400
                ? "Required parameters are missing or have an invalid format"
                : "The request is not valid for this path";
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }
}
