package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.PagedResult;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleSearchCriteria;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.common.exception.InvalidTokenException;
import com.rentamovil.fleetmaintenance.domain.port.in.SearchVehiclesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchVehiclesUseCaseImpl implements SearchVehiclesUseCase {

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 100;
    private static final Set<VehicleStatus> CATALOG = Set.of(VehicleStatus.AVAILABLE);

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final ReservationOccupancyPort reservationOccupancyPort;

    @Override
    public PagedResult<Vehicle> search(AuthenticatedUser caller, VehicleSearchCriteria criteria) {
        Set<VehicleStatus> statuses = criteria.getStatuses() == null || criteria.getStatuses().isEmpty()
                ? CATALOG : criteria.getStatuses();
        //Any status other than AVAILABLE is the admin listing (inventory, statuses...): not public
        boolean adminListing = !statuses.equals(CATALOG);
        if (adminListing) {
            if (caller == null) {
                throw new InvalidTokenException("Missing access token");
            }
            Authorization.require(caller, Permissions.VEHICLES_UPDATE);
        }

        LocalDate from = criteria.getDateFrom();
        LocalDate to = criteria.getDateTo();
        boolean filterByDates = from != null || to != null;
        if (filterByDates) {
            //Occupancy only makes sense for vehicles that can be booked
            if (adminListing) {
                throw new InvalidDataException("status", "dateFrom/dateTo only apply to the catalog (status AVAILABLE)");
            }
            DateRanges.validate(from, to, "dateFrom", "dateTo");
        }
        validatePrices(criteria.getMinPrice(), criteria.getMaxPrice());

        //The public catalog is AVAILABLE only (HU-FLEET-001): RENTED, MAINTENANCE and RETIRED never show up in it
        List<Vehicle> vehicles = vehicleRepositoryPort.search(criteria, statuses);

        //Reservations belong to booking-reservation: it's asked about each vehicle, no local copy
        //(09-microservices/service-catalog.md). The fleet is small, so filtering and paging happen in
        //memory: that way every page holds exactly "limit" free vehicles.
        if (filterByDates) {
            vehicles = vehicles.stream()
                    .filter(vehicle -> !reservationOccupancyPort.isOccupied(vehicle.getId(), from, to))
                    .toList();
        }

        //An empty catalog is not an error (HU-FLEET-001 scenario 2): a page with items = []
        return PagedResult.of(vehicles, page(criteria.getPage()), limit(criteria.getLimit()));
    }

    private static void validatePrices(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidDataException("minPrice", "minPrice can't be greater than maxPrice");
        }
    }

    private static int page(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private static int limit(Integer limit) {
        return limit == null ? DEFAULT_LIMIT : Math.clamp(limit, 1, MAX_LIMIT);
    }
}
