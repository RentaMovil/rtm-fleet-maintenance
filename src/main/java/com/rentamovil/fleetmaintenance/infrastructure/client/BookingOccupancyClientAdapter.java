package com.rentamovil.fleetmaintenance.infrastructure.client;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.rentamovil.fleetmaintenance.domain.exception.DependencyUnavailableException;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;

// Llama a GET booking-reservation:3003/internal/reservations/vehicle-occupancy (booking-reservation.yaml).
// Service to service over the internal network, no JWT, not through the gateway.
// If booking-reservation is down we don't guess: we answer 503 (runbook.md, "Catalog search fails").
@Component
public class BookingOccupancyClientAdapter implements ReservationOccupancyPort {

    private final RestClient restClient;

    public BookingOccupancyClientAdapter(@Value("${app.fleet.booking-url}") String bookingUrl,
                                         @Value("${app.fleet.booking-timeout}") Duration timeout,
                                         @Value("${app.internal.api-key}") String internalApiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder().baseUrl(bookingUrl).requestFactory(requestFactory)
                .defaultHeader("X-Internal-Key", internalApiKey).build();
    }

    @Override
    public boolean isOccupied(Integer vehicleId, LocalDate from, LocalDate to) {
        try {
            OccupancyResponse response = restClient.get()
                    .uri(uri -> uri.path("/internal/reservations/vehicle-occupancy")
                            .queryParam("vehicleId", vehicleId)
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build())
                    .retrieve()
                    .body(OccupancyResponse.class);
            if (response == null || response.occupied() == null) {
                throw new DependencyUnavailableException("BOOKING_UNAVAILABLE",
                        "booking-reservation answered without the occupied field", null);
            }
            return response.occupied();
        } catch (RestClientException e) {
            throw new DependencyUnavailableException("BOOKING_UNAVAILABLE",
                    "Could not check vehicle occupancy in booking-reservation", e);
        }
    }

    record OccupancyResponse(Boolean occupied) {
    }
}
