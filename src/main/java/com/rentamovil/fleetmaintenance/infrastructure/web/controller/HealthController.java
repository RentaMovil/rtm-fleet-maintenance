package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        boolean databaseIsAvailable;
        try (Connection connection = dataSource.getConnection()) {
            databaseIsAvailable = connection.isValid(2);
        } catch (Exception exception) {
            databaseIsAvailable = false;
        }

        Map<String, String> response = Map.of(
                "status", databaseIsAvailable ? "ok" : "degraded",
                "db", databaseIsAvailable ? "connected" : "disconnected");
        HttpStatus status = databaseIsAvailable ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
