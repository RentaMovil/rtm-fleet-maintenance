package com.rentamovil.fleetmaintenance.domain.port.out;

// What the domain needs to store and query vehicles.
// The real implementation (JPA / SQL Server) lives in infrastructure/persistence.
public interface VehicleRepositoryPort {

    boolean existsByBranchId(Integer branchId);
}
