package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;

public interface VehicleMaintenanceRepositoryPort {

    VehicleMaintenance save(VehicleMaintenance maintenance);

    // With the vehicle and the type resolved (to answer the front-end)
    Optional<VehicleMaintenance> findById(Integer id);

    // History: a null filter doesn't filter. Most recent first.
    List<VehicleMaintenance> search(Integer vehicleId, MaintenanceStatus status);

    // The vehicle's IN_PROGRESS maintenance, if any
    Optional<VehicleMaintenance> findInProgressByVehicleId(Integer vehicleId);

    // The vehicle's open ones (SCHEDULED and IN_PROGRESS): cancelled when it's retired (INV-007)
    List<VehicleMaintenance> findOpenByVehicleId(Integer vehicleId);
}
