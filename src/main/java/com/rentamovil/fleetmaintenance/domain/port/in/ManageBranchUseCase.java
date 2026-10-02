package com.rentamovil.fleetmaintenance.domain.port.in;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Branch;

// Create, edit and delete branches (HU-FLEET-007). Requires branches:manage.
public interface ManageBranchUseCase {

    Branch create(AuthenticatedUser caller, BranchData data);

    Branch update(AuthenticatedUser caller, Integer branchId, BranchData data);
}
