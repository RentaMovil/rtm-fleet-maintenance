package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;

import com.rentamovil.fleetmaintenance.domain.port.in.VehicleData;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Body of POST /vehicles and PUT /vehicles/{id} (VehicleWriteRequest). Price and mileage must fit
// DECIMAL(10,2): up to 8 integer digits and 2 decimals.
@Getter
@Setter
public class VehicleWriteRequest {

    @NotBlank
    @Size(max = 10)
    private String plate;

    @NotNull
    private Integer modelId;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotNull
    @Min(1900)
    @Max(2100)
    private Integer year;

    // > 0 (Vehicle.INV-001)
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal dailyPrice;

    @NotNull
    private Integer branchId;

    @DecimalMin("0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal mileage;

    @Size(max = 255)
    private String imageUrl;

    public VehicleData toData() {
        return new VehicleData(plate, modelId, capacity, year, dailyPrice, branchId, mileage, imageUrl);
    }
}
