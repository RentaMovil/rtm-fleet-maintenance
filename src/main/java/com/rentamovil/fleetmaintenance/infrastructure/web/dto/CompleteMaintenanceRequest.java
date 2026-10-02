package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

// (Optional) body of POST /vehicles/{id}/maintenance/complete
@Getter
@Setter
public class CompleteMaintenanceRequest {

    @DecimalMin("0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal cost;
}
