package com.rentamovil.fleetmaintenance.domain.port.in;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;

// Vehicle detail (HU-FLEET-001, HU-FLEET-002). Public.
public interface GetVehicleUseCase {

    Vehicle getVehicle(Integer vehicleId);
}
