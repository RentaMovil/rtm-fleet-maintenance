package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleEntity;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, Integer> {

    // SQL Server is case-insensitive by default: "abc123" and "ABC123" collide
    boolean existsByPlate(String plate);

    boolean existsByBranchId(Integer branchId);
}
