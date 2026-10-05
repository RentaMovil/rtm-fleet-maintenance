package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleMaintenanceEntity;

public interface VehicleMaintenanceJpaRepository extends JpaRepository<VehicleMaintenanceEntity, Integer> {

    // By VehicleMaintenance.INV-001 there is at most one IN_PROGRESS per vehicle
    Optional<VehicleMaintenanceEntity> findFirstByVehicleIdAndStatusOrderByIdDesc(Integer vehicleId, String status);

    // One query for the whole history (vehicle_maintenance only stores ids, so the names are joined here)
    @Query("""
            SELECT m.id AS id, v.id AS vehicleId, v.plate AS plate, b.name AS brandName, mo.name AS modelName,
                   t.id AS maintenanceTypeId, t.name AS maintenanceTypeName,
                   m.startDate AS startDate, m.endDate AS endDate, m.cost AS cost, m.status AS status
            FROM VehicleMaintenanceEntity m, MaintenanceTypeEntity t, VehicleEntity v
            JOIN v.model mo JOIN mo.brand b
            WHERE v.id = m.vehicleId AND t.id = m.maintenanceTypeId
              AND (:vehicleId IS NULL OR m.vehicleId = :vehicleId)
              AND (:status IS NULL OR m.status = :status)
            ORDER BY m.startDate DESC, m.id DESC
            """)
    List<HistoryRow> findHistory(@Param("vehicleId") Integer vehicleId, @Param("status") String status);

    interface HistoryRow {
        Integer getId();
        Integer getVehicleId();
        String getPlate();
        String getBrandName();
        String getModelName();
        Integer getMaintenanceTypeId();
        String getMaintenanceTypeName();
        LocalDate getStartDate();
        LocalDate getEndDate();
        BigDecimal getCost();
        String getStatus();
    }
}
