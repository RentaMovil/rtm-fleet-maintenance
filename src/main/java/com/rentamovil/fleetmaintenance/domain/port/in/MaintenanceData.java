package com.rentamovil.fleetmaintenance.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

// Maintenance data when registering or editing it. When editing, a null field is left untouched.
public record MaintenanceData(Integer maintenanceTypeId, LocalDate startDate, BigDecimal cost,
                              String observations, String imageUrl) {
}
