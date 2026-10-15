package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;

// Sample users and vehicles for the tests
final class TestData {

    // Fleet permissions rtm-iam-db seeds for ADMIN
    static final AuthenticatedUser ADMIN = new AuthenticatedUser(1, "ADMIN", List.of(
            "vehicles:read", "vehicles:create", "vehicles:update", "branches:manage",
            "maintenance:create", "maintenance:update"));

    static final AuthenticatedUser CLIENT = new AuthenticatedUser(3, "CLIENT", List.of("vehicles:read"));

    static final VehicleModel COROLLA = new VehicleModel(7, "Corolla",
            new CatalogItem(1, "Toyota"), new CatalogItem(2, "Sedan"), new CatalogItem(3, "Gasoline"));

    private TestData() {
    }

    static Vehicle vehicle(int id, VehicleStatus status) {
        return Vehicle.builder()
                .id(id)
                .plate("ABC12" + id)
                .model(COROLLA)
                .capacity(5)
                .year(2024)
                .status(status)
                .mileage(new BigDecimal("1000"))
                .dailyPrice(new BigDecimal("150000"))
                .branch(new CatalogItem(4, "Centro"))
                .build();
    }
}
