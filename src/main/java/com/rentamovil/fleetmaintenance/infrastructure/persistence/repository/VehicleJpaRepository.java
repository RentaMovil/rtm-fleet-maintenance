package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleEntity;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, Integer> {

    boolean existsByBranchId(Integer branchId);
}
