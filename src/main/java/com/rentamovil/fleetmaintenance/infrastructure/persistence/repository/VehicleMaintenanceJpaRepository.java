package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleMaintenanceEntity;

public interface VehicleMaintenanceJpaRepository extends JpaRepository<VehicleMaintenanceEntity, Integer> {

    // By VehicleMaintenance.INV-001 there is at most one IN_PROGRESS per vehicle
    Optional<VehicleMaintenanceEntity> findFirstByVehicleIdAndStatusOrderByIdDesc(Integer vehicleId, String status);
}
