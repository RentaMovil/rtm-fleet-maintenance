package com.rentamovil.fleetmaintenance.domain.enums;

public enum MaintenanceStatus {
    // Allowed by the CHECK on fleet.vehicle_maintenance (06-data/models.md), but no user story
    // uses it yet: it's here only so such a row can be read without failing.
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED
}
