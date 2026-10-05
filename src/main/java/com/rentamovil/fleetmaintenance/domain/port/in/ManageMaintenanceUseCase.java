package com.rentamovil.fleetmaintenance.domain.port.in;

import java.math.BigDecimal;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;

// Maintenance management (HU-FLEET-008). Creating requires maintenance:create; editing and
// changing the status, maintenance:update.
public interface ManageMaintenanceUseCase {

    // status: SCHEDULED (planned) or IN_PROGRESS (starts now)
    VehicleMaintenance create(AuthenticatedUser caller, Integer vehicleId, MaintenanceStatus status, MaintenanceData data);

    // Only while it's open (INV-003)
    VehicleMaintenance update(AuthenticatedUser caller, Integer maintenanceId, MaintenanceData data);

    // Start (IN_PROGRESS), complete (COMPLETED) or cancel (CANCELLED). cost only when completing.
    VehicleMaintenance changeStatus(AuthenticatedUser caller, Integer maintenanceId, MaintenanceStatus newStatus,
                                    BigDecimal cost);
}
