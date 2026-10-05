package com.rentamovil.fleetmaintenance.infrastructure.web.dto;

import java.util.List;

import com.rentamovil.fleetmaintenance.domain.port.in.BranchData;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Body of POST /branches and PUT /branches/{id} (BranchWriteRequest)
@Getter
@Setter
public class BranchWriteRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 200)
    private String address;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotBlank
    @Size(max = 20)
    private String phone;

    // The 7 days, Monday to Sunday. No repeats and open-before-close are checked by the use case.
    @NotNull
    @Size(min = 7, max = 7, message = "must contain the 7 days of the week")
    private List<@Valid @NotNull OperatingHourDto> operatingHours;

    public BranchData toData() {
        return new BranchData(name, address, city, phone,
                operatingHours.stream().map(OperatingHourDto::toDomain).toList());
    }
}
