package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.VehicleMaintenanceJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VehicleMaintenanceRepositoryAdapter implements VehicleMaintenanceRepositoryPort {

    private final VehicleMaintenanceJpaRepository vehicleMaintenanceJpaRepository;

    @Override
    public VehicleMaintenance save(VehicleMaintenance maintenance) {
        return PersistenceMapper.toDomain(vehicleMaintenanceJpaRepository.save(PersistenceMapper.toEntity(maintenance)));
    }

    @Override
    public Optional<VehicleMaintenance> findInProgressByVehicleId(Integer vehicleId) {
        return vehicleMaintenanceJpaRepository
                .findFirstByVehicleIdAndStatusOrderByIdDesc(vehicleId, MaintenanceStatus.IN_PROGRESS.name())
                .map(PersistenceMapper::toDomain);
    }
}
