package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;

public interface BranchRepositoryPort {

    // Creates or replaces the branch with its full weekly hours
    Branch save(Branch branch);

    Optional<Branch> findById(Integer id);

    boolean existsById(Integer id);

    List<Branch> findAll();

    void deleteById(Integer id);
}
