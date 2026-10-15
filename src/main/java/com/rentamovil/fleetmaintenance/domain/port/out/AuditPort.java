package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.Map;

// Records relevant actions in shared.audit (who did what, on which record and when)
public interface AuditPort {

    // actorUserId null = the system did it (e.g. reacting to another service's event).
    // detail is stored as JSON, e.g. {"oldStatus": "AVAILABLE", "newStatus": "MAINTENANCE"}
    void record(Integer actorUserId, String action, String affectedTable, Integer affectedRecordId,
                Map<String, Object> detail);
}
