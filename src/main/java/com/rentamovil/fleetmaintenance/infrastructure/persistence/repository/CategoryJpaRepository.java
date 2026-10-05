package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.CategoryEntity;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Integer> {
}
