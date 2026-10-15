package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.fleetmaintenance.domain.port.in.ListReferenceDataUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.CatalogItemResponse;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.VehicleModelResponse;

import lombok.RequiredArgsConstructor;

// Public reference catalogs: search filters and vehicle form options
@RestController
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ListReferenceDataUseCase listReferenceDataUseCase;

    @GetMapping("/brands")
    public List<CatalogItemResponse> brands() {
        return listReferenceDataUseCase.listBrands().stream().map(CatalogItemResponse::from).toList();
    }

    @GetMapping("/categories")
    public List<CatalogItemResponse> categories() {
        return listReferenceDataUseCase.listCategories().stream().map(CatalogItemResponse::from).toList();
    }

    @GetMapping("/engine-types")
    public List<CatalogItemResponse> engineTypes() {
        return listReferenceDataUseCase.listEngineTypes().stream().map(CatalogItemResponse::from).toList();
    }

    @GetMapping("/vehicle-models")
    public List<VehicleModelResponse> vehicleModels() {
        return listReferenceDataUseCase.listVehicleModels().stream().map(VehicleModelResponse::from).toList();
    }

    // Needed so the admin can pick a maintenanceTypeId for
    // PATCH /vehicles/{id}/status (added to fleet-maintenance.yaml with this endpoint)
    @GetMapping("/maintenance-types")
    public List<CatalogItemResponse> maintenanceTypes() {
        return listReferenceDataUseCase.listMaintenanceTypes().stream().map(CatalogItemResponse::from).toList();
    }
}
