package com.rentamovil.fleetmaintenance;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FleetMaintenanceApplication {

    public static void main(String[] args) {
        // Every date in UTC (06-data/modeling-conventions.md): the database is shared between services,
        // so LocalDate.now() must mean the same day wherever the service runs.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(FleetMaintenanceApplication.class, args);
    }
}
