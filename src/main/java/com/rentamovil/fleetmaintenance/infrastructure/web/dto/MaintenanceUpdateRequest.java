package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.port.in.MaintenanceData;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Body of PATCH /maintenances/{id}: only the fields sent change. "" clears observations/imageUrl.
@Getter
@Setter
public class MaintenanceUpdateRequest {

    private Integer maintenanceTypeId;

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
