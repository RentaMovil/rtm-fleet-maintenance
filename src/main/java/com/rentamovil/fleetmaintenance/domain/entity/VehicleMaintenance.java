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

    // Date it was closed (completed or cancelled); null while open
    private LocalDate endDate;
    private BigDecimal cost;
    private String observations;
    private String imageUrl;
    private MaintenanceStatus status;

    // Read-only: filled by the repository on queries, for the history
    private MaintenanceVehicle vehicle;
    private CatalogItem maintenanceType;

    public boolean isClosed() {
        return status.isClosed();
    }
}
