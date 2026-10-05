package com.rentamovil.fleetmaintenance.domain.port.in;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;

// Reference catalogs needed by the search and the vehicle form. Public.
public interface ListReferenceDataUseCase {

    List<CatalogItem> listBrands();

    List<CatalogItem> listCategories();

    List<CatalogItem> listEngineTypes();

    List<VehicleModel> listVehicleModels();

    List<CatalogItem> listMaintenanceTypes();
}
