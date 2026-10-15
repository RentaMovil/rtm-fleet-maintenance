package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.MaintenanceVehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.MaintenanceTypeEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleMaintenanceEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.MaintenanceTypeJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.VehicleJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.VehicleMaintenanceJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleMaintenanceRepositoryAdapter implements VehicleMaintenanceRepositoryPort {

    private static final Set<String> OPEN = Set.of(MaintenanceStatus.SCHEDULED.name(), MaintenanceStatus.IN_PROGRESS.name());

    private final VehicleMaintenanceJpaRepository vehicleMaintenanceJpaRepository;
    private final VehicleJpaRepository vehicleJpaRepository;
    private final MaintenanceTypeJpaRepository maintenanceTypeJpaRepository;

    @Override
    @Transactional
    public VehicleMaintenance save(VehicleMaintenance maintenance) {
        return PersistenceMapper.toDomain(vehicleMaintenanceJpaRepository.save(PersistenceMapper.toEntity(maintenance)));
    }

    @Override
    public Optional<VehicleMaintenance> findById(Integer id) {
        return vehicleMaintenanceJpaRepository.findById(id).map(entity -> withDetails(List.of(entity)).getFirst());
    }

    @Override
    public List<VehicleMaintenance> search(Integer vehicleId, MaintenanceStatus status) {
        return withDetails(vehicleMaintenanceJpaRepository.search(vehicleId, status == null ? null : status.name()));
    }

    @Override
    public Optional<VehicleMaintenance> findInProgressByVehicleId(Integer vehicleId) {
        return vehicleMaintenanceJpaRepository
                .findFirstByVehicleIdAndStatusOrderByIdDesc(vehicleId, MaintenanceStatus.IN_PROGRESS.name())
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<VehicleMaintenance> findOpenByVehicleId(Integer vehicleId) {
        return vehicleMaintenanceJpaRepository.findByVehicleIdAndStatusIn(vehicleId, OPEN).stream()
                .map(PersistenceMapper::toDomain).toList();
    }

    // Resolves vehicle (plate, brand, model) and type name with 2 queries for the whole list,
    // not 2 per maintenance
    private List<VehicleMaintenance> withDetails(List<VehicleMaintenanceEntity> entities) {
        Map<Integer, VehicleEntity> vehicles = vehicleJpaRepository
                .findAllById(entities.stream().map(VehicleMaintenanceEntity::getVehicleId).distinct().toList())
                .stream().collect(Collectors.toMap(VehicleEntity::getId, Function.identity()));
        Map<Integer, MaintenanceTypeEntity> types = maintenanceTypeJpaRepository
                .findAllById(entities.stream().map(VehicleMaintenanceEntity::getMaintenanceTypeId).distinct().toList())
                .stream().collect(Collectors.toMap(MaintenanceTypeEntity::getId, Function.identity()));

        return entities.stream().map(entity -> {
            VehicleMaintenance maintenance = PersistenceMapper.toDomain(entity);
            VehicleEntity vehicle = vehicles.get(entity.getVehicleId());
            maintenance.setVehicle(new MaintenanceVehicle(vehicle.getId(), vehicle.getPlate(),
                    vehicle.getModel().getBrand().getName(), vehicle.getModel().getName()));
            MaintenanceTypeEntity type = types.get(entity.getMaintenanceTypeId());
            maintenance.setMaintenanceType(new CatalogItem(type.getId(), type.getName()));
            return maintenance;
        }).toList();
    }
}
