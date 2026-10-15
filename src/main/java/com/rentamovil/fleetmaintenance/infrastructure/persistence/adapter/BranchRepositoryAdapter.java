package com.rentamovil.fleetmaintenance.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.repository.BranchJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchRepositoryAdapter implements BranchRepositoryPort {

    private final BranchJpaRepository branchJpaRepository;

    @Override
    @Transactional
    public Branch save(Branch branch) {
        BranchEntity entity = branch.getId() == null
                ? new BranchEntity()
                : branchJpaRepository.findWithHoursById(branch.getId()).orElseThrow();
        PersistenceMapper.copyFields(branch, entity);

        //The weekly hours are replaced as a whole. Old rows are deleted first (flush) and then the
        //new ones inserted: Hibernate inserts before deleting by default, and with a unique
        //constraint on (branch_id, day_of_week) the same day would clash with itself.
        if (!entity.getOperatingHours().isEmpty()) {
            entity.getOperatingHours().clear();
            branchJpaRepository.saveAndFlush(entity);
        }
        entity.getOperatingHours().addAll(PersistenceMapper.toOpenHourEntities(branch, entity));

        return PersistenceMapper.toDomain(branchJpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Branch> findById(Integer id) {
        return branchJpaRepository.findWithHoursById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Integer id) {
        return branchJpaRepository.existsById(id);
    }

    @Override
    public List<Branch> findAll() {
        return branchJpaRepository.findAllWithHours().stream().map(PersistenceMapper::toDomain).toList();
    }

    // flush here: if a booking-reservation reservation still points to this branch, the FK stops
    // it now (DataIntegrityViolationException -> 409) instead of at commit time
    @Override
    @Transactional
    public void deleteById(Integer id) {
        branchJpaRepository.deleteById(id);
        branchJpaRepository.flush();
    }
}
