package com.rentamovil.fleetmaintenance.domain.port.in;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;

// Send to maintenance (HU-FLEET-006 scenario 1, requires maintenance:create) or retire
// (HU-FLEET-005 scenarios 2-3, requires vehicles:update). Only MAINTENANCE or RETIRED.
public interface ChangeVehicleStatusUseCase {

    // maintenanceTypeId is only used (and required) when newStatus = MAINTENANCE
    Vehicle changeStatus(AuthenticatedUser caller, Integer vehicleId, VehicleStatus newStatus,
                         Integer maintenanceTypeId);
}
