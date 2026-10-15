package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Envelope of another service's event (02-domain/domain-events.md). Only the fields this service
// uses are read; the rest (aggregateId, version, rentalId, gpsId...) is ignored.
@Getter
@Setter
public class IntegrationEventRequest {

    private String eventId;

    @NotBlank
    private String eventType;

    @NotNull
    @Valid
    private Payload payload;

    @Getter
    @Setter
    public static class Payload {

        @NotNull
        private Integer vehicleId;
    }
}
