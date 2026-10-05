package com.rentamovil.fleetmaintenance.domain.port.in;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;

// Branches with their hours. Public: used by the Home search and the branch info block.
public interface ListBranchesUseCase {

    List<Branch> listBranches();
}
