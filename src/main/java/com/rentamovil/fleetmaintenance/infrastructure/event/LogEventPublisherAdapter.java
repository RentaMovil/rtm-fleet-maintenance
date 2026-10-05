package com.rentamovil.fleetmaintenance.infrastructure.event;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rentamovil.fleetmaintenance.domain.entity.DomainEvent;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

// For now events are only written to the service log (look for "[EVENT]"), using the envelope from
// 02-domain/domain-events.md. There is nobody to deliver them to yet: booking-reservation doesn't exist.
// Once it does, only this adapter is replaced (e.g. a POST to its /internal/events), just like
// LogEmailSenderAdapter in rtm-iam.
@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventPublisherAdapter implements EventPublisherPort {

    private final JsonMapper jsonMapper;

    @Override
    public void publish(DomainEvent event) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", UUID.randomUUID().toString());
        envelope.put("eventType", event.getEventType());
        envelope.put("aggregateId", event.getAggregateId());
        envelope.put("aggregateType", event.getAggregateType());
        envelope.put("occurredAt", Instant.now().toString());
        envelope.put("version", 1);
        envelope.put("payload", event.getPayload());
        log.info("[EVENT] {}", jsonMapper.writeValueAsString(envelope));
    }
}
