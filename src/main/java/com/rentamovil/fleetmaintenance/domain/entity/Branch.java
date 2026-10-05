package com.rentamovil.fleetmaintenance.domain.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    private Integer id;
    private String name;
    private String address;
    private String city;
    private String phone;

    // Map location: both or neither (Branch.INV-001)
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Always the 7 days, Monday to Sunday (closed ones with closed = true)
    @Builder.Default
    private List<OperatingHour> operatingHours = new ArrayList<>();
}
