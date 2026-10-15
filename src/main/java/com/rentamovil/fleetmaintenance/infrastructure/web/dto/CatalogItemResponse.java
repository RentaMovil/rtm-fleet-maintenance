package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;

// CatalogItem in fleet-maintenance.yaml
public record CatalogItemResponse(Integer id, String name) {

    public static CatalogItemResponse from(CatalogItem item) {
        return new CatalogItemResponse(item.getId(), item.getName());
    }
}
