package com.rentamovil.fleetmaintenance.domain.entity;

import java.math.BigDecimal;

import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Aggregate Root of the Fleet & Maintenance context
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    private Integer id;
    private String plate;
    private VehicleModel model;
    private Integer capacity;
    private Integer year;
    private String imageUrl;
    private VehicleStatus status;
    private BigDecimal mileage;
    private BigDecimal dailyPrice;

    // Branch where the vehicle is right now (just id and name)
    private CatalogItem branch;

    public boolean isRetired() {
        return status == VehicleStatus.RETIRED;
    }
}
