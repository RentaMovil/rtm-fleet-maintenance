package com.rentamovil.fleetmaintenance.domain.port.in;

import java.math.BigDecimal;
import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.OperatingHour;

// Branch data when creating or editing it (BranchWriteRequest), with the 7 days of hours
public record BranchData(String name, String address, String city, String phone,
                         BigDecimal latitude, BigDecimal longitude, List<OperatingHour> operatingHours) {
}
