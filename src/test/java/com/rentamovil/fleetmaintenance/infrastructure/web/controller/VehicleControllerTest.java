package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.PagedResult;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
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
import com.rentamovil.fleetmaintenance.domain.port.in.VehicleData;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.ChangeVehicleStatusRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.CompleteMaintenanceRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.VehicleWriteRequest;

/** /vehicles: catalog, detail and availability are public; the rest require a token (HU-FLEET-001 to 006). */
class VehicleControllerTest {

    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(1, "ADMIN",
            List.of("vehicles:create", "vehicles:update", "maintenance:create", "maintenance:update"));

    private static final VehicleModel COROLLA = new VehicleModel(7, "Corolla",
            new CatalogItem(1, "Toyota"), new CatalogItem(2, "Sedan"), new CatalogItem(3, "Gasoline"));

    private final SearchVehiclesUseCase searchVehiclesUseCase = mock(SearchVehiclesUseCase.class);
    private final GetVehicleUseCase getVehicleUseCase = mock(GetVehicleUseCase.class);
    private final CheckVehicleAvailabilityUseCase checkVehicleAvailabilityUseCase =
            mock(CheckVehicleAvailabilityUseCase.class);
    private final RegisterVehicleUseCase registerVehicleUseCase = mock(RegisterVehicleUseCase.class);
    private final UpdateVehicleUseCase updateVehicleUseCase = mock(UpdateVehicleUseCase.class);
    private final ChangeVehicleStatusUseCase changeVehicleStatusUseCase = mock(ChangeVehicleStatusUseCase.class);
    private final CompleteMaintenanceUseCase completeMaintenanceUseCase = mock(CompleteMaintenanceUseCase.class);
    private final GetVehicleStatsUseCase getVehicleStatsUseCase = mock(GetVehicleStatsUseCase.class);
    private final ListInventoryUseCase listInventoryUseCase = mock(ListInventoryUseCase.class);

    private final VehicleController controller = new VehicleController(searchVehiclesUseCase, getVehicleUseCase,
            checkVehicleAvailabilityUseCase, registerVehicleUseCase, updateVehicleUseCase,
            changeVehicleStatusUseCase, completeMaintenanceUseCase, getVehicleStatsUseCase, listInventoryUseCase);

    private static Vehicle vehicle(Integer id, VehicleStatus status) {
        return Vehicle.builder()
                .id(id)
                .plate("ABC123")
                .model(COROLLA)
                .capacity(5)
                .year(2024)
                .status(status)
                .mileage(new BigDecimal("1000"))
                .dailyPrice(new BigDecimal("150000"))
                .branch(new CatalogItem(4, "Centro"))
                .build();
    }

    // --- GET /vehicles: the status query param is turned into a Set<VehicleStatus> ---

    private static Stream<Arguments> statusFilters() {
        return Stream.of(
                Arguments.of("ALL", EnumSet.allOf(VehicleStatus.class)),
                Arguments.of("AVAILABLE", Set.of(VehicleStatus.AVAILABLE)),
                Arguments.of("MAINTENANCE", Set.of(VehicleStatus.MAINTENANCE)));
    }

    @ParameterizedTest
    @MethodSource("statusFilters")
    void statusQueryParamIsTranslatedToTheMatchingStatuses(String status, Set<VehicleStatus> expectedStatuses) {
        when(searchVehiclesUseCase.search(any(), any())).thenReturn(new PagedResult<>(List.of(), 1, 20, 0, 0));

        controller.search(null, status, null, null, null, null, null, null, null, null, null, null);

        verify(searchVehiclesUseCase).search(isNull(), argThatHasStatuses(expectedStatuses));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void withoutStatusTheCatalogDefaultsToAvailableOnly(String status) {
        when(searchVehiclesUseCase.search(any(), any())).thenReturn(new PagedResult<>(List.of(), 1, 20, 0, 0));

        controller.search(null, status, null, null, null, null, null, null, null, null, null, null);

        verify(searchVehiclesUseCase).search(isNull(), argThatHasStatuses(Set.of()));
    }

    @Test
    void anInvalidStatusIsRejectedBeforeCallingTheUseCase() {
        assertThatThrownBy(() -> controller.search(ADMIN, "DELETED", null, null, null, null, null,
                null, null, null, null, null))
                .isInstanceOf(InvalidDataException.class)
                .hasFieldOrPropertyWithValue("field", "status");
    }

    @Test
    void searchMapsThePagedResult() {
        when(searchVehiclesUseCase.search(any(), any()))
                .thenReturn(new PagedResult<>(List.of(vehicle(1, VehicleStatus.AVAILABLE)), 1, 20, 1, 1));

        var response = controller.search(null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(response.items()).extracting(r -> r.id()).containsExactly(1);
        assertThat(response.total()).isEqualTo(1);
    }

    private static VehicleSearchCriteria argThatHasStatuses(Set<VehicleStatus> expected) {
        return argThat(criteria -> criteria.getStatuses().equals(expected));
    }

    // --- The rest of the endpoints ---

    @Test
    void statsDelegatesToTheUseCase() {
        when(getVehicleStatsUseCase.getStats(ADMIN)).thenReturn(Map.of(VehicleStatus.AVAILABLE, 3L));

        assertThat(controller.stats(ADMIN)).containsEntry(VehicleStatus.AVAILABLE, 3L);
    }

    @Test
    void inventoryIsMappedFromTheUseCase() {
        when(listInventoryUseCase.list(ADMIN, VehicleStatus.MAINTENANCE, 4))
                .thenReturn(List.of(vehicle(5, VehicleStatus.MAINTENANCE)));

        var response = controller.inventory(ADMIN, VehicleStatus.MAINTENANCE, 4);

        assertThat(response).extracting(r -> r.id()).containsExactly(5);
    }

    @Test
    void getVehicleIsMappedFromTheUseCase() {
        when(getVehicleUseCase.getVehicle(5)).thenReturn(vehicle(5, VehicleStatus.AVAILABLE));

        assertThat(controller.getVehicle(5).id()).isEqualTo(5);
    }

    @Test
    void availabilityWrapsTheUseCaseResult() {
        LocalDate from = LocalDate.of(2026, 11, 10);
        LocalDate to = LocalDate.of(2026, 11, 15);
        when(checkVehicleAvailabilityUseCase.isAvailable(5, from, to)).thenReturn(true);

        assertThat(controller.availability(5, from, to).available()).isTrue();
    }

    @Test
    void registerDelegatesToTheUseCase() {
        when(registerVehicleUseCase.register(eq(ADMIN), any(VehicleData.class)))
                .thenReturn(vehicle(9, VehicleStatus.AVAILABLE));

        var request = new VehicleWriteRequest();
        request.setPlate("XYZ789");
        request.setModelId(7);
        request.setCapacity(5);
        request.setYear(2024);
        request.setDailyPrice(new BigDecimal("150000"));
        request.setBranchId(4);

        assertThat(controller.register(ADMIN, request).id()).isEqualTo(9);
    }

    @Test
    void updateDelegatesToTheUseCaseWithTheGivenId() {
        when(updateVehicleUseCase.update(eq(ADMIN), eq(9), any(VehicleData.class)))
                .thenReturn(vehicle(9, VehicleStatus.AVAILABLE));

        var request = new VehicleWriteRequest();
        request.setPlate("XYZ789");
        request.setModelId(7);
        request.setCapacity(5);
        request.setYear(2024);
        request.setDailyPrice(new BigDecimal("150000"));
        request.setBranchId(4);

        assertThat(controller.update(ADMIN, 9, request).id()).isEqualTo(9);
    }

    @Test
    void changeStatusParsesTheStatusAndDelegates() {
        when(changeVehicleStatusUseCase.changeStatus(ADMIN, 9, VehicleStatus.MAINTENANCE, 2))
                .thenReturn(vehicle(9, VehicleStatus.MAINTENANCE));

        var request = new ChangeVehicleStatusRequest();
        request.setStatus("MAINTENANCE");
        request.setMaintenanceTypeId(2);

        assertThat(controller.changeStatus(ADMIN, 9, request).status()).isEqualTo("MAINTENANCE");
    }

    @Test
    void completeMaintenanceWithACostInTheBody() {
        when(completeMaintenanceUseCase.complete(ADMIN, 9, new BigDecimal("50000")))
                .thenReturn(vehicle(9, VehicleStatus.AVAILABLE));

        var request = new CompleteMaintenanceRequest();
        request.setCost(new BigDecimal("50000"));

        assertThat(controller.completeMaintenance(ADMIN, 9, request).status()).isEqualTo("AVAILABLE");
    }

    @Test
    void completeMaintenanceWithoutABodyUsesNoCost() {
        when(completeMaintenanceUseCase.complete(ADMIN, 9, null)).thenReturn(vehicle(9, VehicleStatus.AVAILABLE));

        controller.completeMaintenance(ADMIN, 9, null);

        verify(completeMaintenanceUseCase).complete(ADMIN, 9, null);
    }
}
