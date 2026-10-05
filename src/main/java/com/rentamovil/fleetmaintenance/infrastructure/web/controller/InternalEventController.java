package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.in.HandleRentalEventUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.IntegrationEventRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Entry point for other services' events (HTTP callback, events.md: "no message broker needed").
// Like booking-reservation's /internal/reservations/vehicle-occupancy: service to service over the
// internal network, no JWT. The gateway doesn't route /internal/**, so this is unreachable from outside.
@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalEventController {

    private final HandleRentalEventUseCase handleRentalEventUseCase;

    @PostMapping("/internal/events")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@Valid @RequestBody IntegrationEventRequest event) {
        Integer vehicleId = event.getPayload().getVehicleId();
        log.info("Event received: {} eventId={} vehicleId={}", event.getEventType(), event.getEventId(), vehicleId);

        switch (event.getEventType()) {
            case "RentalStarted" -> handleRentalEventUseCase.rentalStarted(vehicleId);
            case "RentalCompleted" -> handleRentalEventUseCase.rentalCompleted(vehicleId);
            default -> throw new InvalidDataException("eventType",
                    "This service does not consume " + event.getEventType());
        }
    }
}
