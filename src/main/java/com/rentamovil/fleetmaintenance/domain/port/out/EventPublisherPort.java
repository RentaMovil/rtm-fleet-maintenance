package com.rentamovil.fleetmaintenance.domain.port.out;

import com.rentamovil.fleetmaintenance.domain.entity.DomainEvent;

// Publishes this service's events (VehicleSentToMaintenance, MaintenanceCompleted)
public interface EventPublisherPort {

    void publish(DomainEvent event);
}
