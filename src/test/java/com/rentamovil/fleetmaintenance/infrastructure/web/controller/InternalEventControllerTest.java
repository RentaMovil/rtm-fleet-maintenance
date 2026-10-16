package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.in.HandleRentalEventUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.IntegrationEventRequest;

/** POST /internal/events: dispatches booking-reservation's rental events by eventType. */
class InternalEventControllerTest {

    private final HandleRentalEventUseCase useCase = mock(HandleRentalEventUseCase.class);
    private final InternalEventController controller = new InternalEventController(useCase);

    private static IntegrationEventRequest event(String eventType, Integer vehicleId) {
        var payload = new IntegrationEventRequest.Payload();
        payload.setVehicleId(vehicleId);

        var request = new IntegrationEventRequest();
        request.setEventId("evt-1");
        request.setEventType(eventType);
        request.setPayload(payload);
        return request;
    }

    @Test
    void rentalStartedDelegatesToTheUseCase() {
        controller.receive(event("RentalStarted", 5));

        verify(useCase).rentalStarted(5);
    }

    @Test
    void rentalCompletedDelegatesToTheUseCase() {
        controller.receive(event("RentalCompleted", 5));

        verify(useCase).rentalCompleted(5);
    }

    @Test
    void unknownEventTypeIsRejectedAndNeverReachesTheUseCase() {
        assertThatThrownBy(() -> controller.receive(event("RentalCancelled", 5)))
                .isInstanceOf(InvalidDataException.class)
                .hasFieldOrPropertyWithValue("field", "eventType");

        verifyNoInteractions(useCase);
    }
}
