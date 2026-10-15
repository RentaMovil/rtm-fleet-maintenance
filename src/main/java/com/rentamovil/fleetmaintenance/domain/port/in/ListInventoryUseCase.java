package com.rentamovil.fleetmaintenance.domain.port.in;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;

// The whole fleet in any status, for the admin inventory and status screens. The public catalog
// (SearchVehiclesUseCase) only ever shows AVAILABLE vehicles.
public interface ListInventoryUseCase {

    List<Vehicle> list(AuthenticatedUser caller, VehicleStatus status, Integer branchId);
}
