package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BrandEntity;

public interface BrandJpaRepository extends JpaRepository<BrandEntity, Integer> {
}
