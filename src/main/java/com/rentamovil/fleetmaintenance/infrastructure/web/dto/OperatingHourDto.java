package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rentamovil.fleetmaintenance.domain.entity.OperatingHour;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// OperatingHour in fleet-maintenance.yaml. Times look like "08:00". A closed day: closed = true and no times.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperatingHourDto {

    // 1 = Monday ... 7 = Sunday
    @NotNull
    @Min(1)
    @Max(7)
    private Integer dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime opensAt;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closesAt;

    // Optional (default false). Boolean, not boolean: Jackson 3 rejects the body when a primitive is missing
    private Boolean closed;

    public OperatingHour toDomain() {
        return new OperatingHour(dayOfWeek, opensAt, closesAt, Boolean.TRUE.equals(closed));
    }

    public static OperatingHourDto from(OperatingHour hour) {
        return new OperatingHourDto(hour.getDayOfWeek(), hour.getOpensAt(), hour.getClosesAt(), hour.isClosed());
    }
}
