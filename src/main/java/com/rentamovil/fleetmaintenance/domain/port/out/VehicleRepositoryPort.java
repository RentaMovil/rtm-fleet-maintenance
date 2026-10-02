package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.Optional;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;

// What the domain needs to store and query vehicles.
// The real implementation (JPA / SQL Server) lives in infrastructure/persistence.
public interface VehicleRepositoryPort {

    // Saves and returns the full vehicle (with model, brand, category... resolved)
    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(Integer id);

    boolean existsByPlate(String plate);

    boolean existsByBranchId(Integer branchId);
}
