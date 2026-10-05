package com.rentamovil.fleetmaintenance.domain.port.in;

import java.math.BigDecimal;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;

// Close the maintenance in progress and put the vehicle back to AVAILABLE (HU-FLEET-006 scenario 4).
// Requires maintenance:update.
public interface CompleteMaintenanceUseCase {

    Vehicle complete(AuthenticatedUser caller, Integer vehicleId, BigDecimal cost);
}
