package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchEntity;

public interface BranchJpaRepository extends JpaRepository<BranchEntity, Integer> {

    // With the hours loaded at once (LAZY so that listing vehicles doesn't fetch them)
    @EntityGraph(attributePaths = "operatingHours")
    @Query("SELECT b FROM BranchEntity b ORDER BY b.name")
    List<BranchEntity> findAllWithHours();

    @EntityGraph(attributePaths = "operatingHours")
    @Query("SELECT b FROM BranchEntity b WHERE b.id = :id")
    Optional<BranchEntity> findWithHoursById(@Param("id") Integer id);
}
