package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.VehicleJpaRepository;

import lombok.RequiredArgsConstructor;

// Every method maps to the domain inside its transaction, so model, brand and branch can be
// read even when they are lazy references (open-in-view is off)
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleRepositoryAdapter implements VehicleRepositoryPort {

    private final VehicleJpaRepository vehicleJpaRepository;

    @Override
    public boolean existsByBranchId(Integer branchId) {
        return vehicleJpaRepository.existsByBranchId(branchId);
    }
}
