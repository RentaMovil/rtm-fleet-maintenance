package com.rentamovil.fleetmaintenance.application.usecase;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.entity.OperatingHour;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.in.BranchData;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageBranchUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ManageBranchUseCaseImpl implements ManageBranchUseCase {

    private final BranchRepositoryPort branchRepositoryPort;
    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final AuditPort auditPort;

    @Override
    @Transactional
    public Branch create(AuthenticatedUser caller, BranchData data) {
        Authorization.require(caller, Permissions.BRANCHES_MANAGE);

        Branch saved = branchRepositoryPort.save(toBranch(null, data));
        auditPort.record(caller.getUserId(), "BRANCH_CREATED", "branch", saved.getId(), Map.of("name", saved.getName()));
        return saved;
    }

    @Override
    @Transactional
    public Branch update(AuthenticatedUser caller, Integer branchId, BranchData data) {
        Authorization.require(caller, Permissions.BRANCHES_MANAGE);

        if (!branchRepositoryPort.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found: " + branchId);
        }

        Branch saved = branchRepositoryPort.save(toBranch(branchId, data));
        auditPort.record(caller.getUserId(), "BRANCH_UPDATED", "branch", branchId, Map.of("name", saved.getName()));
        return saved;
    }

    @Override
    @Transactional
    public void delete(AuthenticatedUser caller, Integer branchId) {
        Authorization.require(caller, Permissions.BRANCHES_MANAGE);

        Branch branch = branchRepositoryPort.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));

        //HU-FLEET-007 scenario 2. RETIRED vehicles count too: their row still points to the branch
        if (vehicleRepositoryPort.existsByBranchId(branchId)) {
            throw new BusinessRuleException("BRANCH_HAS_VEHICLES",
                    "The branch has vehicles assigned: reassign them first");
        }

        branchRepositoryPort.deleteById(branchId);
        auditPort.record(caller.getUserId(), "BRANCH_DELETED", "branch", branchId, Map.of("name", branch.getName()));
    }

    private static Branch toBranch(Integer id, BranchData data) {
        return Branch.builder()
                .id(id)
                .name(data.name().trim())
                .address(data.address().trim())
                .city(data.city().trim())
                .phone(data.phone() == null ? null : data.phone().trim())
                .latitude(data.latitude())
                .longitude(data.longitude())
                .operatingHours(validateWeek(data.operatingHours()))
                .build();
    }

    // Exactly the 7 days, one per day (BranchOperatingHour.INV-002), and every open day
    // opens before it closes (BranchOperatingHour.INV-001)
    static List<OperatingHour> validateWeek(List<OperatingHour> hours) {
        if (hours == null || hours.size() != 7) {
            throw new InvalidDataException("operatingHours", "All 7 days of the week are required");
        }

        Set<Integer> days = new HashSet<>();
        for (OperatingHour hour : hours) {
            if (hour.getDayOfWeek() < 1 || hour.getDayOfWeek() > 7) {
                throw new InvalidDataException("operatingHours", "dayOfWeek goes from 1 (Monday) to 7 (Sunday)");
            }
            if (!days.add(hour.getDayOfWeek())) {
                throw new InvalidDataException("operatingHours", "Day " + hour.getDayOfWeek() + " is repeated");
            }
            if (hour.isClosed()) {
                continue;
            }
            if (hour.getOpensAt() == null || hour.getClosesAt() == null) {
                throw new InvalidDataException("operatingHours",
                        "Day " + hour.getDayOfWeek() + " needs opensAt and closesAt, or closed = true");
            }
            if (!hour.getOpensAt().isBefore(hour.getClosesAt())) {
                throw new InvalidDataException("operatingHours",
                        "Day " + hour.getDayOfWeek() + " must open before it closes");
            }
        }

        //A closed day has no hours (and no row is stored for it)
        return hours.stream()
                .map(hour -> hour.isClosed()
                        ? new OperatingHour(hour.getDayOfWeek(), null, null, true)
                        : hour)
                .sorted(Comparator.comparingInt(OperatingHour::getDayOfWeek))
                .toList();
    }
}
