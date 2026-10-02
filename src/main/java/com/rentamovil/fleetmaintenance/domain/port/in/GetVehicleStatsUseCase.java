package com.rentamovil.fleetmaintenance.domain.port.in;

import java.util.Map;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;

// How many vehicles are in each of the 4 statuses (HU-FLEET-006 scenario 3). Administrators only.
public interface GetVehicleStatsUseCase {

    Map<VehicleStatus, Long> getStats(AuthenticatedUser caller);
}
