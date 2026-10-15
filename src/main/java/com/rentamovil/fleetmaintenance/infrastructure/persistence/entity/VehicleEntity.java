package com.rentamovil.fleetmaintenance.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// fleet.vehicle: Aggregate Root of the context. Never deleted (RETIRED is a logical retirement).
@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Integer id;

    @Column(name = "plate", nullable = false, length = 10)
    private String plate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModelEntity model;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "year")
    private Integer year;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "mileage", nullable = false, precision = 10, scale = 2)
    private BigDecimal mileage;

    @Column(name = "daily_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyPrice;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branch;
}
