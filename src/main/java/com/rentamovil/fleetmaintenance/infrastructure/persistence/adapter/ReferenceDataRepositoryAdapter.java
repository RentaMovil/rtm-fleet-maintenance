package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.BrandJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.CategoryJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.EngineTypeJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.MaintenanceTypeJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.VehicleModelJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferenceDataRepositoryAdapter implements ReferenceDataRepositoryPort {

    private static final Sort BY_NAME = Sort.by("name");

    private final BrandJpaRepository brandJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final EngineTypeJpaRepository engineTypeJpaRepository;
    private final VehicleModelJpaRepository vehicleModelJpaRepository;
    private final MaintenanceTypeJpaRepository maintenanceTypeJpaRepository;

    @Override
    public List<CatalogItem> findAllBrands() {
        return brandJpaRepository.findAll(BY_NAME).stream()
                .map(brand -> new CatalogItem(brand.getId(), brand.getName())).toList();
    }

    @Override
    public List<CatalogItem> findAllCategories() {
        return categoryJpaRepository.findAll(BY_NAME).stream()
                .map(category -> new CatalogItem(category.getId(), category.getName())).toList();
    }

    @Override
    public List<CatalogItem> findAllEngineTypes() {
        return engineTypeJpaRepository.findAll(BY_NAME).stream()
                .map(engineType -> new CatalogItem(engineType.getId(), engineType.getName())).toList();
    }

    @Override
    public List<VehicleModel> findAllVehicleModels() {
        return vehicleModelJpaRepository.findAllWithDetails().stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<CatalogItem> findAllMaintenanceTypes() {
        return maintenanceTypeJpaRepository.findAll(BY_NAME).stream()
                .map(type -> new CatalogItem(type.getId(), type.getName())).toList();
    }

    @Override
    public Optional<VehicleModel> findVehicleModelById(Integer id) {
        return vehicleModelJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public boolean existsMaintenanceType(Integer id) {
        return maintenanceTypeJpaRepository.existsById(id);
    }
}
