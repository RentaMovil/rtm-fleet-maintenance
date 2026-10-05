package com.rentamovil.fleetmaintenance.domain.entity;

// This service's permissions, exactly as rtm-iam-db seeds them (00-governance/security-policy.md).
// ADMIN and SUPER_ADMIN have them; a CLIENT only has vehicles:read.
public final class Permissions {

    public static final String VEHICLES_CREATE = "vehicles:create";
    public static final String VEHICLES_UPDATE = "vehicles:update";
    public static final String BRANCHES_MANAGE = "branches:manage";
    public static final String MAINTENANCE_CREATE = "maintenance:create";
    public static final String MAINTENANCE_UPDATE = "maintenance:update";

    private Permissions() {
    }
}
