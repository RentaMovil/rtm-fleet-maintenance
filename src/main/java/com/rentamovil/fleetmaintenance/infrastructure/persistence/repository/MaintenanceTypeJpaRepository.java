package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.MaintenanceTypeEntity;

public interface MaintenanceTypeJpaRepository extends JpaRepository<MaintenanceTypeEntity, Integer> {
}
