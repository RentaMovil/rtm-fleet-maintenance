package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;

// Vehicle in fleet-maintenance.yaml. imageUrl is null when there's no photo (nullable in the contract).
public record VehicleResponse(Integer id, String plate, VehicleModelResponse model, Integer capacity, Integer year,
                              String imageUrl, String status, BigDecimal mileage, BigDecimal dailyPrice,
                              CatalogItemResponse branch) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getId(), vehicle.getPlate(), VehicleModelResponse.from(vehicle.getModel()),
                vehicle.getCapacity(), vehicle.getYear(), vehicle.getImageUrl(), vehicle.getStatus().name(),
                vehicle.getMileage(), vehicle.getDailyPrice(), CatalogItemResponse.from(vehicle.getBranch()));
    }
}
