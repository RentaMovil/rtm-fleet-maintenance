package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.AuditEntity;

public interface AuditJpaRepository extends JpaRepository<AuditEntity, Integer> {
}
