package com.rentamovil.fleetmaintenance.domain.port.in;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;

// Maintenance history (HU-FLEET-008 scenario 5). Requires maintenance:update.
public interface ListMaintenancesUseCase {

    List<VehicleMaintenance> list(AuthenticatedUser caller, Integer vehicleId, MaintenanceStatus status);
}
