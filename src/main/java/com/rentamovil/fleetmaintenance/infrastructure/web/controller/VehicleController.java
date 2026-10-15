package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleSearchCriteria;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.in.ChangeVehicleStatusUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.CheckVehicleAvailabilityUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.CompleteMaintenanceUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.GetVehicleStatsUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.GetVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ListInventoryUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.RegisterVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.SearchVehiclesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.UpdateVehicleUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.auth.CurrentUser;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.AvailabilityResponse;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.ChangeVehicleStatusRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.CompleteMaintenanceRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.VehiclePageResponse;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.VehicleResponse;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.VehicleWriteRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-FLEET-001 to 006. Catalog, detail and availability are public; the rest require a token and
// permissions are checked in each use case.
@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final SearchVehiclesUseCase searchVehiclesUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final CheckVehicleAvailabilityUseCase checkVehicleAvailabilityUseCase;
    private final RegisterVehicleUseCase registerVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final ChangeVehicleStatusUseCase changeVehicleStatusUseCase;
    private final CompleteMaintenanceUseCase completeMaintenanceUseCase;
    private final GetVehicleStatsUseCase getVehicleStatsUseCase;
    private final ListInventoryUseCase listInventoryUseCase;

    @GetMapping
    // No status (or AVAILABLE): public catalog. Any other status: admin listing, needs a token (use case)
    public VehiclePageResponse search(@CurrentUser(required = false) AuthenticatedUser currentUser,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
                                      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
                                      @RequestParam(required = false) Integer branchId,
                                      @RequestParam(required = false) Integer categoryId,
                                      @RequestParam(required = false) Integer brandId,
                                      @RequestParam(required = false) Integer engineTypeId,
                                      @RequestParam(required = false) BigDecimal minPrice,
                                      @RequestParam(required = false) BigDecimal maxPrice,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer limit) {
        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder()
                .dateFrom(dateFrom).dateTo(dateTo)
                .branchId(branchId).categoryId(categoryId).brandId(brandId).engineTypeId(engineTypeId)
                .minPrice(minPrice).maxPrice(maxPrice)
                .statuses(toStatuses(status))
                .page(page).limit(limit)
                .build();
        return VehiclePageResponse.from(searchVehiclesUseCase.search(currentUser, criteria));
    }

    // Placed before /{id} only for readability: Spring always prefers the literal path
    @GetMapping("/stats")
    public Map<VehicleStatus, Long> stats(@CurrentUser AuthenticatedUser currentUser) {
        return getVehicleStatsUseCase.getStats(currentUser);
    }

    // Admin inventory: every status, not paged (the fleet is small). Same data as GET /vehicles?status=ALL
    @GetMapping("/inventory")
    public List<VehicleResponse> inventory(@CurrentUser AuthenticatedUser currentUser,
                                           @RequestParam(required = false) VehicleStatus status,
                                           @RequestParam(required = false) Integer branchId) {
        return listInventoryUseCase.list(currentUser, status, branchId).stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicle(@PathVariable Integer id) {
        return VehicleResponse.from(getVehicleUseCase.getVehicle(id));
    }

    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(@PathVariable Integer id,
                                             @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                             @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        return new AvailabilityResponse(checkVehicleAvailabilityUseCase.isAvailable(id, from, to));
    }

    // ALL = every status; empty = the catalog (AVAILABLE)
    private static Set<VehicleStatus> toStatuses(String status) {
        if (status == null || status.isBlank()) {
            return Set.of();
        }
        if ("ALL".equals(status)) {
            return EnumSet.allOf(VehicleStatus.class);
        }
        try {
            return Set.of(VehicleStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("status", "must be AVAILABLE, RENTED, MAINTENANCE, RETIRED or ALL");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse register(@CurrentUser AuthenticatedUser currentUser,
                                    @Valid @RequestBody VehicleWriteRequest request) {
        return VehicleResponse.from(registerVehicleUseCase.register(currentUser, request.toData()));
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@CurrentUser AuthenticatedUser currentUser,
                                  @PathVariable Integer id,
                                  @Valid @RequestBody VehicleWriteRequest request) {
        return VehicleResponse.from(updateVehicleUseCase.update(currentUser, id, request.toData()));
    }

    @PatchMapping("/{id}/status")
    public VehicleResponse changeStatus(@CurrentUser AuthenticatedUser currentUser,
                                        @PathVariable Integer id,
                                        @Valid @RequestBody ChangeVehicleStatusRequest request) {
        return VehicleResponse.from(changeVehicleStatusUseCase.changeStatus(currentUser, id,
                VehicleStatus.valueOf(request.getStatus()), request.getMaintenanceTypeId()));
    }

    // The body is optional: without it the maintenance is closed with no cost
    @PostMapping("/{id}/maintenance/complete")
    public VehicleResponse completeMaintenance(@CurrentUser AuthenticatedUser currentUser,
                                               @PathVariable Integer id,
                                               @Valid @RequestBody(required = false) CompleteMaintenanceRequest request) {
        return VehicleResponse.from(completeMaintenanceUseCase.complete(currentUser, id,
                request == null ? null : request.getCost()));
    }
}
