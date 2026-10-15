package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// Body of PATCH /maintenances/{id}/status. Cancelling replaces deleting (VehicleMaintenance.INV-005).
@Getter
@Setter
public class MaintenanceStatusRequest {

    @NotBlank
    @Pattern(regexp = "IN_PROGRESS|COMPLETED|CANCELLED", message = "must be IN_PROGRESS, COMPLETED or CANCELLED")
    private String status;

    // Only used when completing
    @DecimalMin("0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal cost;
}
