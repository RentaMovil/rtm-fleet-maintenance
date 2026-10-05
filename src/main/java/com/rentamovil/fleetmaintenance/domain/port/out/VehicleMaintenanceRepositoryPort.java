package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.rentamovil.fleetmaintenance.domain.entity.MaintenanceRecord;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;

public interface VehicleMaintenanceRepositoryPort {

    VehicleMaintenance save(VehicleMaintenance maintenance);

    // The vehicle's IN_PROGRESS maintenance, if any
    Optional<VehicleMaintenance> findInProgressByVehicleId(Integer vehicleId);

    // History with vehicle and type names, newest first. Null filters are ignored
    List<MaintenanceRecord> findHistory(Integer vehicleId, MaintenanceStatus status);
}
