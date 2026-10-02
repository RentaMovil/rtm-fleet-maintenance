package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.port.in.ListBranchesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListBranchesUseCaseImpl implements ListBranchesUseCase {

    private final BranchRepositoryPort branchRepositoryPort;

    @Override
    public List<Branch> listBranches() {
        return branchRepositoryPort.findAll();
    }
}
