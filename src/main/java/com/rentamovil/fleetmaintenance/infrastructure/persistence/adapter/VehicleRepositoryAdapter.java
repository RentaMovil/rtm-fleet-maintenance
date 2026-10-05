package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleSearchCriteria;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.BranchJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.VehicleJpaRepository;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.VehicleModelJpaRepository;

import lombok.RequiredArgsConstructor;

// Every method maps to the domain inside its transaction, so model, brand and branch can be
// read even when they are lazy references (open-in-view is off)
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleRepositoryAdapter implements VehicleRepositoryPort {

    private final VehicleJpaRepository vehicleJpaRepository;
    private final VehicleModelJpaRepository vehicleModelJpaRepository;
    private final BranchJpaRepository branchJpaRepository;

    @Override
    @Transactional
    public Vehicle save(Vehicle vehicle) {
        //getReferenceById doesn't hit the database: the use case already checked model and branch exist
        var entity = PersistenceMapper.toEntity(vehicle,
                vehicleModelJpaRepository.getReferenceById(vehicle.getModel().getId()),
                branchJpaRepository.getReferenceById(vehicle.getBranch().getId()));
        var saved = vehicleJpaRepository.saveAndFlush(entity);
        //Read it back to return brand, category, branch names...
        return vehicleJpaRepository.findById(saved.getId()).map(PersistenceMapper::toDomain).orElseThrow();
    }

    @Override
    public Optional<Vehicle> findById(Integer id) {
        return vehicleJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByPlate(String plate) {
        return vehicleJpaRepository.existsByPlate(plate);
    }

    @Override
    public List<Vehicle> searchAvailable(VehicleSearchCriteria criteria) {
        return vehicleJpaRepository.search(VehicleStatus.AVAILABLE.name(),
                        criteria.getBranchId(), criteria.getCategoryId(), criteria.getBrandId(),
                        criteria.getEngineTypeId(), criteria.getMinPrice(), criteria.getMaxPrice())
                .stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public boolean existsByBranchId(Integer branchId) {
        return vehicleJpaRepository.existsByBranchId(branchId);
    }
}
