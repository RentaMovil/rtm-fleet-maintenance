package com.rentamovil.fleetmaintenance.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BranchOperatingHourEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.BrandEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.CategoryEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.EngineTypeEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleMaintenanceEntity;
import com.rentamovil.fleetmaintenance.infrastructure.persistence.entity.VehicleModelEntity;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fleet-maintenance-test;MODE=MSSQLServer;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC"
})
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FleetJpaRepositoryTest {

    @Autowired
    private BranchJpaRepository branchRepository;

    @Autowired
    private BrandJpaRepository brandRepository;

    @Autowired
    private CategoryJpaRepository categoryRepository;

    @Autowired
    private EngineTypeJpaRepository engineTypeRepository;

    @Autowired
    private VehicleJpaRepository vehicleRepository;

    @Autowired
    private VehicleMaintenanceJpaRepository maintenanceRepository;

    @Autowired
    private VehicleModelJpaRepository vehicleModelRepository;

    @Test
    void branchQueriesReturnSortedBranchesAndFetchOperatingHours() {
        BranchEntity norte = saveBranch("Norte", true);
        BranchEntity centro = saveBranch("Centro", true);

        List<BranchEntity> branches = branchRepository.findAllWithHours();

        assertThat(branches).extracting(BranchEntity::getName).containsExactly("Centro", "Norte");
        assertThat(branches.get(0).getOperatingHours()).singleElement()
                .satisfies(hour -> assertThat(hour.getDayOfWeek()).isEqualTo(1));
        assertThat(branchRepository.findWithHoursById(norte.getId()))
                .get()
                .extracting(BranchEntity::getOperatingHours)
                .asList()
                .hasSize(1);
        assertThat(centro.getId()).isNotNull();
    }

    @Test
    void vehicleQueriesApplyOptionalFiltersAndGroupStatuses() {
        BranchEntity branch = saveBranch("Centro", false);
        VehicleModelEntity model = saveModel("Toyota", "Corolla", "Sedan", "Gasoline");
        VehicleEntity available = saveVehicle("ABC123", "AVAILABLE", "150000", branch, model);
        saveVehicle("XYZ789", "RENTED", "220000", branch, model);

        List<VehicleEntity> availableVehicles = vehicleRepository.search(
                List.of("AVAILABLE"), null, null, null, null, null, null);
        List<VehicleEntity> aboveMinimumPrice = vehicleRepository.search(
                List.of("AVAILABLE", "RENTED"), null, null, null, null, new BigDecimal("200000"), null);
        Map<String, Long> counts = vehicleRepository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));

        assertThat(availableVehicles).extracting(VehicleEntity::getId).containsExactly(available.getId());
        assertThat(aboveMinimumPrice).extracting(VehicleEntity::getPlate).containsExactly("XYZ789");
        assertThat(counts).containsEntry("AVAILABLE", 1L).containsEntry("RENTED", 1L);
        assertThat(vehicleRepository.existsByPlate("ABC123")).isTrue();
        assertThat(vehicleRepository.existsByBranchId(branch.getId())).isTrue();
    }

    @Test
    void maintenanceSearchAppliesOptionalFiltersAndSortsNewestFirst() {
        VehicleMaintenanceEntity older = saveMaintenance(5, "COMPLETED", LocalDate.of(2026, 4, 2));
        VehicleMaintenanceEntity newer = saveMaintenance(5, "IN_PROGRESS", LocalDate.of(2026, 4, 12));
        saveMaintenance(8, "IN_PROGRESS", LocalDate.of(2026, 4, 20));

        List<VehicleMaintenanceEntity> all = maintenanceRepository.search(null, null);
        List<VehicleMaintenanceEntity> vehicleHistory = maintenanceRepository.search(5, null);
        List<VehicleMaintenanceEntity> inProgress = maintenanceRepository.search(5, "IN_PROGRESS");

        assertThat(all).extracting(VehicleMaintenanceEntity::getId)
                .containsExactly(inProgressId(8), newer.getId(), older.getId());
        assertThat(vehicleHistory).extracting(VehicleMaintenanceEntity::getId)
                .containsExactly(newer.getId(), older.getId());
        assertThat(inProgress).extracting(VehicleMaintenanceEntity::getId).containsExactly(newer.getId());
    }

    @Test
    void vehicleModelQueryFetchesCatalogDetailsAndSortsByBrandThenName() {
        saveModel("Toyota", "Yaris", "Hatchback", "Hybrid");
        VehicleModelEntity corolla = saveModel("Toyota", "Corolla", "Sedan", "Gasoline");
        saveModel("Honda", "Civic", "Sedan", "Gasoline");

        List<VehicleModelEntity> models = vehicleModelRepository.findAllWithDetails();

        assertThat(models).extracting(model -> model.getBrand().getName() + " " + model.getName())
                .containsExactly("Honda Civic", "Toyota Corolla", "Toyota Yaris");
        assertThat(models.get(1).getId()).isEqualTo(corolla.getId());
        assertThat(models.get(1).getCategory().getName()).isEqualTo("Sedan");
        assertThat(models.get(1).getEngineType().getName()).isEqualTo("Gasoline");
    }

    private BranchEntity saveBranch(String name, boolean withOperatingHours) {
        BranchEntity branch = new BranchEntity();
        branch.setName(name);
        branch.setAddress("Calle 1");
        branch.setCity("Neiva");
        if (withOperatingHours) {
            BranchOperatingHourEntity hour = new BranchOperatingHourEntity();
            hour.setBranch(branch);
            hour.setDayOfWeek(1);
            hour.setOpensAt(LocalTime.of(8, 0));
            hour.setClosesAt(LocalTime.of(17, 0));
            branch.setOperatingHours(List.of(hour));
        }
        return branchRepository.save(branch);
    }

    private VehicleModelEntity saveModel(String brandName, String modelName, String categoryName,
            String engineTypeName) {
        BrandEntity brand = new BrandEntity();
        brand.setName(brandName);
        brand = brandRepository.save(brand);

        CategoryEntity category = new CategoryEntity();
        category.setName(categoryName);
        category = categoryRepository.save(category);

        EngineTypeEntity engineType = new EngineTypeEntity();
        engineType.setName(engineTypeName);
        engineType = engineTypeRepository.save(engineType);

        VehicleModelEntity model = new VehicleModelEntity();
        model.setBrand(brand);
        model.setName(modelName);
        model.setCategory(category);
        model.setEngineType(engineType);
        return vehicleModelRepository.save(model);
    }

    private VehicleEntity saveVehicle(String plate, String status, String dailyPrice,
            BranchEntity branch, VehicleModelEntity model) {
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setPlate(plate);
        vehicle.setStatus(status);
        vehicle.setMileage(BigDecimal.ZERO);
        vehicle.setDailyPrice(new BigDecimal(dailyPrice));
        vehicle.setBranch(branch);
        vehicle.setModel(model);
        return vehicleRepository.save(vehicle);
    }

    private VehicleMaintenanceEntity saveMaintenance(int vehicleId, String status, LocalDate startDate) {
        VehicleMaintenanceEntity maintenance = new VehicleMaintenanceEntity();
        maintenance.setVehicleId(vehicleId);
        maintenance.setMaintenanceTypeId(1);
        maintenance.setStatus(status);
        maintenance.setStartDate(startDate);
        return maintenanceRepository.save(maintenance);
    }

    private Integer inProgressId(int vehicleId) {
        return maintenanceRepository.findFirstByVehicleIdAndStatusOrderByIdDesc(vehicleId, "IN_PROGRESS")
                .orElseThrow()
                .getId();
    }
}