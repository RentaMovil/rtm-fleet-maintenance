package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.port.in.MaintenanceData;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Body of POST /maintenances (MaintenanceCreateRequest). Date rules depend on the status: use case.
@Getter
@Setter
public class MaintenanceCreateRequest {

    @NotNull
    private Integer vehicleId;

    @NotNull
    private Integer maintenanceTypeId;

    @NotBlank
    @Pattern(regexp = "SCHEDULED|IN_PROGRESS", message = "must be SCHEDULED or IN_PROGRESS")
    private String status;

    private LocalDate startDate;

    @DecimalMin("0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal cost;

    @Size(max = 500)
    private String observations;

    @Size(max = 255)
    private String imageUrl;

    public MaintenanceData toData() {
        return new MaintenanceData(maintenanceTypeId, startDate, cost, observations, imageUrl);
    }
}
