package com.rentamovil.fleetmaintenance.domain.port.in;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;

// Edit a vehicle, full replacement (HU-FLEET-005 scenario 1). Requires vehicles:update.
public interface UpdateVehicleUseCase {

    Vehicle update(AuthenticatedUser caller, Integer vehicleId, VehicleData data);
}
