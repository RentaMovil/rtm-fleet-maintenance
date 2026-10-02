package com.rentamovil.fleetmaintenance.domain.entity;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Opening hours of one weekday of a branch (fleet.branch_operating_hour).
// A closed day has no row in the table: here it shows up as closed = true, without hours.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatingHour {

    // 1 = Monday ... 7 = Sunday
    private int dayOfWeek;
    private LocalTime opensAt;
    private LocalTime closesAt;
    private boolean closed;
}
