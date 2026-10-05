package com.rentamovil.fleetmaintenance.application.usecase;

import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;

// Date range [from, to): from inclusive, to exclusive (the return frees the vehicle that same day)
final class DateRanges {

    private DateRanges() {
    }

    static void validate(LocalDate from, LocalDate to, String fromField, String toField) {
        if (from == null) {
            throw new InvalidDataException(fromField, fromField + " is required together with " + toField);
        }
        if (to == null) {
            throw new InvalidDataException(toField, toField + " is required together with " + fromField);
        }
        if (!from.isBefore(to)) {
            throw new InvalidDataException(toField, toField + " must be after " + fromField);
        }
    }
}
