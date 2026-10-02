package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.PagedResult;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;

// VehiclePagedResult: PaginatedMeta (_shared.yaml) + items. No results is items = [], never an error.
public record VehiclePageResponse(List<VehicleResponse> items, int page, int limit, long total, int totalPages) {

    public static VehiclePageResponse from(PagedResult<Vehicle> result) {
        return new VehiclePageResponse(result.getItems().stream().map(VehicleResponse::from).toList(),
                result.getPage(), result.getLimit(), result.getTotal(), result.getTotalPages());
    }
}
