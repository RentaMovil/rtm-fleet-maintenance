package com.rentamovil.fleetmaintenance.infrastructure.persistence.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// fleet.branch_operating_hour: hours of one day. Closed days have no row.
@Entity
@Table(name = "branch_operating_hour")
@Getter
@Setter
@NoArgsConstructor
public class BranchOperatingHourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_operating_hour_id")
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branch;

    // The column is SMALLINT; without columnDefinition Hibernate expects INT and validation fails
    @Column(name = "day_of_week", nullable = false, columnDefinition = "smallint")
    private Integer dayOfWeek;

    @Column(name = "opens_at", nullable = false)
    private LocalTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private LocalTime closesAt;
}
