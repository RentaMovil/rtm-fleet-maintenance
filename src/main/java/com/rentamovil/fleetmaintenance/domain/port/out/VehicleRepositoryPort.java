package com.rentamovil.fleetmaintenance.domain.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleSearchCriteria;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;

// What the domain needs to store and query vehicles.
// The real implementation (JPA / SQL Server) lives in infrastructure/persistence.
public interface VehicleRepositoryPort {

    // Saves and returns the full vehicle (with model, brand, category... resolved)
    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(Integer id);

    boolean existsByPlate(String plate);

    // AVAILABLE vehicles matching the catalog filters (except dates: booking-reservation
    // resolves those). Ordered by id so paging is stable.
    List<Vehicle> searchAvailable(VehicleSearchCriteria criteria);

    Map<VehicleStatus, Long> countByStatus();

    boolean existsByBranchId(Integer branchId);
}
