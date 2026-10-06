package com.rentamovil.fleetmaintenance.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// fleet.vehicle_maintenance. Stores the vehicle and type ids: this service never navigates
// from a maintenance to its vehicle
@Entity
@Table(name = "vehicle_maintenance")
@Getter
@Setter
@NoArgsConstructor
public class VehicleMaintenanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_id")
    private Integer id;

    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;

    @Column(name = "maintenance_type_id", nullable = false)
    private Integer maintenanceTypeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "observations", length = 500)
    private String observations;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "status", nullable = false, length = 30)
    private String status;
}
