package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.EnumMap;
import java.util.Map;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.port.in.GetVehicleStatsUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetVehicleStatsUseCaseImpl implements GetVehicleStatsUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public Map<VehicleStatus, Long> getStats(AuthenticatedUser caller) {
        //There is no "vehicles:stats" permission: the panel belongs to whoever manages the fleet (vehicles:update).
        //vehicles:read is not enough, because a CLIENT has it too.
        Authorization.require(caller, Permissions.VEHICLES_UPDATE);

        //All 4 statuses are always present, even with 0 vehicles
        Map<VehicleStatus, Long> stats = new EnumMap<>(VehicleStatus.class);
        for (VehicleStatus status : VehicleStatus.values()) {
            stats.put(status, 0L);
        }
        stats.putAll(vehicleRepositoryPort.countByStatus());
        return stats;
    }
}
