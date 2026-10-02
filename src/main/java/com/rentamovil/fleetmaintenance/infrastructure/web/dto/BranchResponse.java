package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;

public record BranchResponse(Integer id, String name, String address, String city, String phone,
                             List<OperatingHourDto> operatingHours) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getName(), branch.getAddress(), branch.getCity(),
                branch.getPhone(), branch.getOperatingHours().stream().map(OperatingHourDto::from).toList());
    }
}
