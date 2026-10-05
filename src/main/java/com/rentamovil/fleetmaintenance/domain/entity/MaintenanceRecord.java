package com.rentamovil.fleetmaintenance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// One row of the maintenance history: the maintenance plus what the panel shows of its vehicle
// and type, so the frontend doesn't have to cross three lists
@Getter
@AllArgsConstructor
@Builder
public class MaintenanceRecord {

    private Integer id;
    private Integer vehicleId;
    private String plate;
    private String vehicleName;
    private Integer maintenanceTypeId;
    private String maintenanceTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal cost;
    private MaintenanceStatus status;
}
