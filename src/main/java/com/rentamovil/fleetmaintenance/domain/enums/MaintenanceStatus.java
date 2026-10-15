package com.rentamovil.fleetmaintenance.domain.enums;

// Maintenance lifecycle (02-domain/entities-and-rules.md -> VehicleMaintenance)
public enum MaintenanceStatus {
    // Planned for a date; the vehicle keeps operating
    SCHEDULED,
    // In the workshop; the vehicle is in MAINTENANCE
    IN_PROGRESS,
    COMPLETED,
    // Replaces deleting: a maintenance is never removed (INV-005)
    CANCELLED;

    // COMPLETED and CANCELLED are terminal and read-only (INV-003)
    public boolean isClosed() {
        return this == COMPLETED || this == CANCELLED;
    }
}
