package com.rentamovil.fleetmaintenance.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Item of a simple catalog (brand, category, engine type, maintenance type)
// or a short reference to another entity (e.g. a vehicle's branch: just id and name)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogItem {

    private Integer id;
    private String name;
}
