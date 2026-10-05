package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rentamovil.fleetmaintenance.domain.entity.MaintenanceRecord;
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

    @Override
    public List<MaintenanceRecord> findHistory(Integer vehicleId, MaintenanceStatus status) {
        return vehicleMaintenanceJpaRepository.findHistory(vehicleId, status == null ? null : status.name()).stream()
                .map(row -> MaintenanceRecord.builder()
                        .id(row.getId())
                        .vehicleId(row.getVehicleId())
                        .plate(row.getPlate())
                        .vehicleName(row.getBrandName() + " " + row.getModelName())
                        .maintenanceTypeId(row.getMaintenanceTypeId())
                        .maintenanceTypeName(row.getMaintenanceTypeName())
                        .startDate(row.getStartDate())
                        .endDate(row.getEndDate())
                        .cost(row.getCost())
                        .status(MaintenanceStatus.valueOf(row.getStatus()))
                        .build())
                .toList();
    }
}
