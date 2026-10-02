package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleModelEntity;

// Converts between domain entities and JPA entities (tables)
public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    public static VehicleModel toDomain(VehicleModelEntity entity) {
        return VehicleModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .brand(new CatalogItem(entity.getBrand().getId(), entity.getBrand().getName()))
                .category(new CatalogItem(entity.getCategory().getId(), entity.getCategory().getName()))
                .engineType(new CatalogItem(entity.getEngineType().getId(), entity.getEngineType().getName()))
                .build();
    }
}
