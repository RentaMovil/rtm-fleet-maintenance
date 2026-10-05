package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// Body of PATCH /vehicles/{id}/status. AVAILABLE is not accepted: it has its own paths (decisions.md).
@Getter
@Setter
public class ChangeVehicleStatusRequest {

    @NotBlank
    @Pattern(regexp = "MAINTENANCE|RETIRED", message = "must be MAINTENANCE or RETIRED")
    private String status;

    // Required when status = MAINTENANCE (checked by the use case)
    private Integer maintenanceTypeId;
}
