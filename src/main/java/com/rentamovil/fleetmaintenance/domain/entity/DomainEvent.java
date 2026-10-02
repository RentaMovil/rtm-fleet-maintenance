package com.rentamovil.fleetmaintenance.domain.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Something that happened in the business that other services may need to know
// (format in 02-domain/domain-events.md). eventId and occurredAt are set by the publisher.
@Getter
@AllArgsConstructor
public class DomainEvent {

    public static final String VEHICLE_SENT_TO_MAINTENANCE = "VehicleSentToMaintenance";
    public static final String MAINTENANCE_COMPLETED = "MaintenanceCompleted";

    private String eventType;
    private String aggregateType;
    private Integer aggregateId;
    private Map<String, Object> payload;
}
