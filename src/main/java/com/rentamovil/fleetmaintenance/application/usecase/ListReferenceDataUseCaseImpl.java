package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.domain.port.in.ListReferenceDataUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListReferenceDataUseCaseImpl implements ListReferenceDataUseCase {

    private final ReferenceDataRepositoryPort referenceDataRepositoryPort;

    @Override
    public List<CatalogItem> listBrands() {
        return referenceDataRepositoryPort.findAllBrands();
    }

    @Override
    public List<CatalogItem> listCategories() {
        return referenceDataRepositoryPort.findAllCategories();
    }

    @Override
    public List<CatalogItem> listEngineTypes() {
        return referenceDataRepositoryPort.findAllEngineTypes();
    }

    @Override
    public List<VehicleModel> listVehicleModels() {
        return referenceDataRepositoryPort.findAllVehicleModels();
    }

    @Override
    public List<CatalogItem> listMaintenanceTypes() {
        return referenceDataRepositoryPort.findAllMaintenanceTypes();
    }
}
