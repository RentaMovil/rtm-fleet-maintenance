package com.rentamovil.fleetmaintenance.domain.port.in;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.MaintenanceRecord;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;

// Maintenance history for the admin panel, newest first. Both filters are optional.
public interface ListMaintenancesUseCase {

    List<MaintenanceRecord> list(AuthenticatedUser caller, Integer vehicleId, MaintenanceStatus status);
}
