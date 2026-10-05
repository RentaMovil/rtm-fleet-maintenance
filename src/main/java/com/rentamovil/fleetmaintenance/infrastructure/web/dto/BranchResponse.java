package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;

public record BranchResponse(Integer id, String name, String address, String city, String phone,
                             BigDecimal latitude, BigDecimal longitude,
                             List<OperatingHourDto> operatingHours) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getName(), branch.getAddress(), branch.getCity(),
                branch.getPhone(), branch.getLatitude(), branch.getLongitude(),
                branch.getOperatingHours().stream().map(OperatingHourDto::from).toList());
    }
}
