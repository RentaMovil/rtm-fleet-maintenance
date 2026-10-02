package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.in.VehicleData;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;

import lombok.RequiredArgsConstructor;

// Shared by register and edit: normalize the plate and check that the model and the
// branch in the request exist
@RequiredArgsConstructor
public class VehicleDataResolver {

    private final ReferenceDataRepositoryPort referenceDataRepositoryPort;
    private final BranchRepositoryPort branchRepositoryPort;

    // "abc 123 " and "ABC123" are the same physical plate
    static String normalizePlate(String plate) {
        return plate.replaceAll("\\s", "").toUpperCase();
    }

    VehicleModel resolveModel(VehicleData data) {
        return referenceDataRepositoryPort.findVehicleModelById(data.modelId())
                .orElseThrow(() -> new InvalidDataException("modelId", "Vehicle model not found: " + data.modelId()));
    }

    CatalogItem resolveBranch(VehicleData data) {
        if (!branchRepositoryPort.existsById(data.branchId())) {
            throw new InvalidDataException("branchId", "Branch not found: " + data.branchId());
        }
        return new CatalogItem(data.branchId(), null);
    }

    static BigDecimal mileageOrZero(BigDecimal mileage) {
        return mileage == null ? BigDecimal.ZERO : mileage;
    }
}
