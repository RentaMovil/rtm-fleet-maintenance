package com.rentamovil.fleetmaintenance.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.rentamovil.fleetmaintenance.domain.entity.PagedResult;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleSearchCriteria;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.in.SearchVehiclesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchVehiclesUseCaseImpl implements SearchVehiclesUseCase {

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 100;

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final ReservationOccupancyPort reservationOccupancyPort;

    @Override
    public PagedResult<Vehicle> search(VehicleSearchCriteria criteria) {
        LocalDate from = criteria.getDateFrom();
        LocalDate to = criteria.getDateTo();
        boolean filterByDates = from != null || to != null;
        if (filterByDates) {
            DateRanges.validate(from, to, "dateFrom", "dateTo");
        }
        validatePrices(criteria.getMinPrice(), criteria.getMaxPrice());

        //AVAILABLE only (HU-FLEET-001): RENTED, MAINTENANCE and RETIRED never show up in the catalog
        List<Vehicle> vehicles = vehicleRepositoryPort.searchAvailable(criteria);

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
