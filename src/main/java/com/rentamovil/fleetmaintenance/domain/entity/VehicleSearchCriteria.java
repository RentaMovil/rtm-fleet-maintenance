package com.rentamovil.fleetmaintenance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

// Catalog filters (HU-FLEET-001, HU-FLEET-003). All optional.
@Getter
@Builder
public class VehicleSearchCriteria {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Integer branchId;
    private Integer categoryId;
    private Integer brandId;
    private Integer engineTypeId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer page;
    private Integer limit;
}
