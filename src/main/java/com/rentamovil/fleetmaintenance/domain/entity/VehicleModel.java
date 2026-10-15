package com.rentamovil.fleetmaintenance.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// A brand's model. Category and engine type belong to the model, not to each vehicle
// ("a Corolla is a gasoline Sedan"): the admin picks a model instead of combining category + engine.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModel {

    private Integer id;
    private String name;
    private CatalogItem brand;
    private CatalogItem category;
    private CatalogItem engineType;
}
