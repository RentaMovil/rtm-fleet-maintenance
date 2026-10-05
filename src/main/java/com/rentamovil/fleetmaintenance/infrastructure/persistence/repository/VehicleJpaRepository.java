package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleEntity;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, Integer> {

    // SQL Server is case-insensitive by default: "abc123" and "ABC123" collide
    boolean existsByPlate(String plate);

    boolean existsByBranchId(Integer branchId);

    // Catalog: a null filter doesn't filter. Category and engine are read through the model.
    @Query("""
            SELECT v FROM VehicleEntity v
            JOIN FETCH v.model m JOIN FETCH m.brand JOIN FETCH m.category JOIN FETCH m.engineType
            JOIN FETCH v.branch
            WHERE v.status = :status
              AND (:branchId IS NULL OR v.branch.id = :branchId)
              AND (:categoryId IS NULL OR m.category.id = :categoryId)
              AND (:brandId IS NULL OR m.brand.id = :brandId)
              AND (:engineTypeId IS NULL OR m.engineType.id = :engineTypeId)
              AND (:minPrice IS NULL OR v.dailyPrice >= :minPrice)
              AND (:maxPrice IS NULL OR v.dailyPrice <= :maxPrice)
            ORDER BY v.id
            """)
    List<VehicleEntity> search(@Param("status") String status,
                               @Param("branchId") Integer branchId,
                               @Param("categoryId") Integer categoryId,
                               @Param("brandId") Integer brandId,
                               @Param("engineTypeId") Integer engineTypeId,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice);
}
