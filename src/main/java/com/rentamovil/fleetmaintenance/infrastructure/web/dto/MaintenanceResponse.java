package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;

// Maintenance in fleet-maintenance.yaml
public record MaintenanceResponse(Integer id, VehicleSummary vehicle, CatalogItemResponse maintenanceType,
                                  LocalDate startDate, LocalDate endDate, BigDecimal cost, String observations,
                                  String imageUrl, String status) {

    public record VehicleSummary(Integer id, String plate, String brand, String model) {
    }

    public static MaintenanceResponse from(VehicleMaintenance maintenance) {
        var vehicle = maintenance.getVehicle();
        return new MaintenanceResponse(maintenance.getId(),
                new VehicleSummary(vehicle.id(), vehicle.plate(), vehicle.brand(), vehicle.model()),
                CatalogItemResponse.from(maintenance.getMaintenanceType()),
                maintenance.getStartDate(), maintenance.getEndDate(), maintenance.getCost(),
                maintenance.getObservations(), maintenance.getImageUrl(), maintenance.getStatus().name());
    }
}
