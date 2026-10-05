package com.rentamovil.fleetmaintenance.domain.port.in;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;

// Register a vehicle (HU-FLEET-004). Requires vehicles:create.
public interface RegisterVehicleUseCase {

    Vehicle register(AuthenticatedUser caller, VehicleData data);
}
