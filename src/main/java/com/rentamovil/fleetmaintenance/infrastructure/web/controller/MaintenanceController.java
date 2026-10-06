package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.port.in.ListMaintenancesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageMaintenanceUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.auth.CurrentUser;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceCreateRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceResponse;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceStatusRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-FLEET-008. There is no DELETE: cancelling replaces deleting (VehicleMaintenance.INV-005).
@RestController
@RequestMapping("/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final ListMaintenancesUseCase listMaintenancesUseCase;
    private final ManageMaintenanceUseCase manageMaintenanceUseCase;

    @GetMapping
    public List<MaintenanceResponse> list(@CurrentUser AuthenticatedUser currentUser,
                                          @RequestParam(required = false) Integer vehicleId,
                                          @RequestParam(required = false) MaintenanceStatus status) {
        return listMaintenancesUseCase.list(currentUser, vehicleId, status).stream()
                .map(MaintenanceResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponse create(@CurrentUser AuthenticatedUser currentUser,
                                      @Valid @RequestBody MaintenanceCreateRequest request) {
        return MaintenanceResponse.from(manageMaintenanceUseCase.create(currentUser, request.getVehicleId(),
                MaintenanceStatus.valueOf(request.getStatus()), request.toData()));
    }

    @PatchMapping("/{id}")
    public MaintenanceResponse update(@CurrentUser AuthenticatedUser currentUser,
                                      @PathVariable Integer id,
                                      @Valid @RequestBody MaintenanceUpdateRequest request) {
        return MaintenanceResponse.from(manageMaintenanceUseCase.update(currentUser, id, request.toData()));
    }

    @PatchMapping("/{id}/status")
    public MaintenanceResponse changeStatus(@CurrentUser AuthenticatedUser currentUser,
                                            @PathVariable Integer id,
                                            @Valid @RequestBody MaintenanceStatusRequest request) {
        return MaintenanceResponse.from(manageMaintenanceUseCase.changeStatus(currentUser, id,
                MaintenanceStatus.valueOf(request.getStatus()), request.getCost()));
    }
}
