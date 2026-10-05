package com.rentamovil.fleetmaintenance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleMaintenance {

    private Integer id;
    private Integer vehicleId;
    private Integer maintenanceTypeId;
    private LocalDate startDate;

    // null while in progress
    private LocalDate endDate;
    private BigDecimal cost;
    private MaintenanceStatus status;
}
