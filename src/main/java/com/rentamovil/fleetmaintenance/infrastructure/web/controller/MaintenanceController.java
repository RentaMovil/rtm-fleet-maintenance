package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.port.in.ListMaintenancesUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.auth.CurrentUser;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceResponse;

import lombok.RequiredArgsConstructor;

// Read side of maintenances (admin "History" screen). Opening and closing a maintenance stay on the
// vehicle: PATCH /vehicles/{id}/status (MAINTENANCE) and POST /vehicles/{id}/maintenance/complete.
@RestController
@RequestMapping("/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final ListMaintenancesUseCase listMaintenancesUseCase;

    @GetMapping
    public List<MaintenanceResponse> list(@CurrentUser AuthenticatedUser currentUser,
                                          @RequestParam(required = false) Integer vehicleId,
                                          @RequestParam(required = false) MaintenanceStatus status) {
        return listMaintenancesUseCase.list(currentUser, vehicleId, status).stream()
                .map(MaintenanceResponse::from)
                .toList();
    }
}
