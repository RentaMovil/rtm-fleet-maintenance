package com.rentamovil.fleetmaintenance.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// fleet.engine_type: Gasoline, Diesel, Electric, Hybrid
@Entity
@Table(name = "engine_type")
@Getter
@Setter
@NoArgsConstructor
public class EngineTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "engine_type_id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;
}
