package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;

// Reference catalogs: brands, categories, engine types, models and maintenance types.
// Read-only for now; they are loaded by rtm-fleet-maintenance-db.
public interface ReferenceDataRepositoryPort {

    List<CatalogItem> findAllBrands();

    List<CatalogItem> findAllCategories();

    List<CatalogItem> findAllEngineTypes();

    List<VehicleModel> findAllVehicleModels();

    List<CatalogItem> findAllMaintenanceTypes();

    Optional<VehicleModel> findVehicleModelById(Integer id);

    boolean existsMaintenanceType(Integer id);
}
