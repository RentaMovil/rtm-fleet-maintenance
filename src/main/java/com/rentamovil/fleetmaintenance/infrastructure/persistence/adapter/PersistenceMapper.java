package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.OperatingHour;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchOperatingHourEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleMaintenanceEntity;
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

    public static Vehicle toDomain(VehicleEntity entity) {
        return Vehicle.builder()
                .id(entity.getId())
                .plate(entity.getPlate())
                .model(toDomain(entity.getModel()))
                .capacity(entity.getCapacity())
                .year(entity.getYear())
                .imageUrl(entity.getImageUrl())
                .status(VehicleStatus.valueOf(entity.getStatus()))
                .mileage(entity.getMileage())
                .dailyPrice(entity.getDailyPrice())
                //Just id and name: the branch hours aren't needed to show a vehicle
                .branch(new CatalogItem(entity.getBranch().getId(), entity.getBranch().getName()))
                .build();
    }

    // model and branch come already resolved (JPA references to existing rows)
    public static VehicleEntity toEntity(Vehicle vehicle, VehicleModelEntity model, BranchEntity branch) {
        VehicleEntity entity = new VehicleEntity();
        entity.setId(vehicle.getId());
        entity.setPlate(vehicle.getPlate());
        entity.setModel(model);
        entity.setCapacity(vehicle.getCapacity());
        entity.setYear(vehicle.getYear());
        entity.setImageUrl(vehicle.getImageUrl());
        entity.setStatus(vehicle.getStatus().name());
        entity.setMileage(vehicle.getMileage());
        entity.setDailyPrice(vehicle.getDailyPrice());
        entity.setBranch(branch);
        return entity;
    }

    public static VehicleMaintenance toDomain(VehicleMaintenanceEntity entity) {
        return VehicleMaintenance.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicleId())
                .maintenanceTypeId(entity.getMaintenanceTypeId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .cost(entity.getCost())
                .status(MaintenanceStatus.valueOf(entity.getStatus()))
                .build();
    }

    public static VehicleMaintenanceEntity toEntity(VehicleMaintenance maintenance) {
        VehicleMaintenanceEntity entity = new VehicleMaintenanceEntity();
        entity.setId(maintenance.getId());
        entity.setVehicleId(maintenance.getVehicleId());
        entity.setMaintenanceTypeId(maintenance.getMaintenanceTypeId());
        entity.setStartDate(maintenance.getStartDate());
        entity.setEndDate(maintenance.getEndDate());
        entity.setCost(maintenance.getCost());
        entity.setStatus(maintenance.getStatus().name());
        return entity;
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
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
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
        entity.setLatitude(branch.getLatitude());
        entity.setLongitude(branch.getLongitude());
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
