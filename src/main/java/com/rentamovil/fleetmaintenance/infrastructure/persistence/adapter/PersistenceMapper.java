package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.OperatingHour;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchOperatingHourEntity;
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

    // Always returns the 7 days: those without a row in the table are closed
    public static Branch toDomain(BranchEntity entity) {
        Map<Integer, BranchOperatingHourEntity> byDay = entity.getOperatingHours().stream()
                .collect(Collectors.toMap(BranchOperatingHourEntity::getDayOfWeek, Function.identity(), (a, b) -> a));

        List<OperatingHour> week = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            BranchOperatingHourEntity hour = byDay.get(day);
            week.add(hour == null
                    ? new OperatingHour(day, null, null, true)
                    : new OperatingHour(day, hour.getOpensAt(), hour.getClosesAt(), false));
        }

        return Branch.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .phone(entity.getPhone())
                .operatingHours(week)
                .build();
    }

    // Copies the branch fields onto the entity (new or already loaded). The hour rows are
    // added separately: see BranchRepositoryAdapter.save
    public static void copyFields(Branch branch, BranchEntity entity) {
        entity.setName(branch.getName());
        entity.setAddress(branch.getAddress());
        entity.setCity(branch.getCity());
        entity.setPhone(branch.getPhone());
    }

    // Only open days get a row (opens_at and closes_at are NOT NULL)
    public static List<BranchOperatingHourEntity> toOpenHourEntities(Branch branch, BranchEntity owner) {
        return branch.getOperatingHours().stream()
                .filter(hour -> !hour.isClosed())
                .map(hour -> {
                    BranchOperatingHourEntity entity = new BranchOperatingHourEntity();
                    entity.setBranch(owner);
                    entity.setDayOfWeek(hour.getDayOfWeek());
                    entity.setOpensAt(hour.getOpensAt());
                    entity.setClosesAt(hour.getClosesAt());
                    return entity;
                })
                .toList();
    }
}
