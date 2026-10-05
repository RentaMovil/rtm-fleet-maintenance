package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.port.in.RegisterVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.UpdateVehicleUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.auth.CurrentUser;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.VehicleResponse;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.VehicleWriteRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-FLEET-001 to 006. Catalog, detail and availability are public; the rest require a token and
// permissions are checked in each use case.
@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final RegisterVehicleUseCase registerVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse register(@CurrentUser AuthenticatedUser currentUser,
                                    @Valid @RequestBody VehicleWriteRequest request) {
        return VehicleResponse.from(registerVehicleUseCase.register(currentUser, request.toData()));
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@CurrentUser AuthenticatedUser currentUser,
                                  @PathVariable Integer id,
                                  @Valid @RequestBody VehicleWriteRequest request) {
        return VehicleResponse.from(updateVehicleUseCase.update(currentUser, id, request.toData()));
    }
}
