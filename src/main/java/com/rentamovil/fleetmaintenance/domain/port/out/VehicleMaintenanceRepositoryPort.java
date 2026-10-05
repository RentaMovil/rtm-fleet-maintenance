package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.Optional;

import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;

public interface VehicleMaintenanceRepositoryPort {

    VehicleMaintenance save(VehicleMaintenance maintenance);

    // The vehicle's IN_PROGRESS maintenance, if any
    Optional<VehicleMaintenance> findInProgressByVehicleId(Integer vehicleId);
}
