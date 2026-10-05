package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.entity.MaintenanceRecord;

public record MaintenanceResponse(Integer id, Integer vehicleId, String plate, String vehicleName,
                                  CatalogItemResponse maintenanceType, LocalDate startDate, LocalDate endDate,
                                  BigDecimal cost, String status) {

    public static MaintenanceResponse from(MaintenanceRecord record) {
        return new MaintenanceResponse(record.getId(), record.getVehicleId(), record.getPlate(), record.getVehicleName(),
                new CatalogItemResponse(record.getMaintenanceTypeId(), record.getMaintenanceTypeName()),
                record.getStartDate(), record.getEndDate(), record.getCost(), record.getStatus().name());
    }
}
