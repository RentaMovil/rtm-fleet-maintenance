package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;

// Sample users and vehicles for the tests
final class TestData {

    // Fleet permissions rtm-iam-db seeds for ADMIN
    static final AuthenticatedUser ADMIN = new AuthenticatedUser(1, "ADMIN", List.of(
            "vehicles:read", "vehicles:create", "vehicles:update", "branches:manage",
            "maintenance:create", "maintenance:update"));

    static final AuthenticatedUser CLIENT = new AuthenticatedUser(3, "CLIENT", List.of("vehicles:read"));

    private TestData() {
    }
}
