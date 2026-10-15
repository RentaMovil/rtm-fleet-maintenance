package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleMaintenanceEntity;

public interface VehicleMaintenanceJpaRepository extends JpaRepository<VehicleMaintenanceEntity, Integer> {

    // By VehicleMaintenance.INV-001 there is at most one IN_PROGRESS per vehicle
    Optional<VehicleMaintenanceEntity> findFirstByVehicleIdAndStatusOrderByIdDesc(Integer vehicleId, String status);

    List<VehicleMaintenanceEntity> findByVehicleIdAndStatusIn(Integer vehicleId, Collection<String> statuses);

    // History: a null filter doesn't filter. Most recent first.
    @Query("""
            SELECT m FROM VehicleMaintenanceEntity m
            WHERE (:vehicleId IS NULL OR m.vehicleId = :vehicleId)
              AND (:status IS NULL OR m.status = :status)
            ORDER BY m.startDate DESC, m.id DESC
            """)
    List<VehicleMaintenanceEntity> search(@Param("vehicleId") Integer vehicleId, @Param("status") String status);
}
