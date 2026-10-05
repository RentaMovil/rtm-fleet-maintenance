package com.rentamovil.fleetmaintenance.domain.port.in;

import java.math.BigDecimal;

// Vehicle data when registering or editing it (VehicleWriteRequest in fleet-maintenance.yaml).
// The status is not here: it's created AVAILABLE and only changes through its own paths.
public record VehicleData(String plate, Integer modelId, Integer capacity, Integer year,
                          BigDecimal dailyPrice, Integer branchId, BigDecimal mileage, String imageUrl) {
}
