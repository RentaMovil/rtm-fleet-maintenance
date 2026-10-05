package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;

public record VehicleModelResponse(Integer id, String name, CatalogItemResponse brand,
                                   CatalogItemResponse category, CatalogItemResponse engineType) {

    public static VehicleModelResponse from(VehicleModel model) {
        return new VehicleModelResponse(model.getId(), model.getName(),
                CatalogItemResponse.from(model.getBrand()),
                CatalogItemResponse.from(model.getCategory()),
                CatalogItemResponse.from(model.getEngineType()));
    }
}
