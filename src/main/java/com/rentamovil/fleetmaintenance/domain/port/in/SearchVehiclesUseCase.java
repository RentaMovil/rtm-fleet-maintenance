package com.rentamovil.fleetmaintenance.domain.port.in;

import com.rentamovil.fleetmaintenance.domain.entity.PagedResult;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleSearchCriteria;

// Public catalog with filters (HU-FLEET-001, HU-FLEET-003). AVAILABLE vehicles only.
public interface SearchVehiclesUseCase {

    PagedResult<Vehicle> search(VehicleSearchCriteria criteria);
}
