package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.EngineTypeEntity;

public interface EngineTypeJpaRepository extends JpaRepository<EngineTypeEntity, Integer> {
}
