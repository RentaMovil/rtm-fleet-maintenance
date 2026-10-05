package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleModelEntity;

public interface VehicleModelJpaRepository extends JpaRepository<VehicleModelEntity, Integer> {

    // Brand, category and engine in the same query (no extra query per model)
    @Query("""
            SELECT m FROM VehicleModelEntity m
            JOIN FETCH m.brand JOIN FETCH m.category JOIN FETCH m.engineType
            ORDER BY m.brand.name, m.name
            """)
    List<VehicleModelEntity> findAllWithDetails();
}
