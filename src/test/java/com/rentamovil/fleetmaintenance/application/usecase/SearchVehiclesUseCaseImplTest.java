package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.PagedResult;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleSearchCriteria;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class SearchVehiclesUseCaseImplTest {

    private static final LocalDate FROM = LocalDate.of(2026, 11, 10);
    private static final LocalDate TO = LocalDate.of(2026, 11, 15);

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private ReservationOccupancyPort reservationOccupancyPort;

    @InjectMocks
    private SearchVehiclesUseCaseImpl searchVehiclesUseCase;

    @Test
    void withDatesExcludesOccupiedVehicles() {
        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder().dateFrom(FROM).dateTo(TO).build();
        when(vehicleRepositoryPort.searchAvailable(criteria)).thenReturn(List.of(
                TestData.vehicle(1, VehicleStatus.AVAILABLE), TestData.vehicle(2, VehicleStatus.AVAILABLE)));
        when(reservationOccupancyPort.isOccupied(1, FROM, TO)).thenReturn(true);
        when(reservationOccupancyPort.isOccupied(2, FROM, TO)).thenReturn(false);

        PagedResult<Vehicle> result = searchVehiclesUseCase.search(criteria);

        assertThat(result.getItems()).extracting(Vehicle::getId).containsExactly(2);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void withoutDatesBookingIsNotAsked() {
        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder().categoryId(2).build();
        when(vehicleRepositoryPort.searchAvailable(criteria)).thenReturn(List.of(TestData.vehicle(1, VehicleStatus.AVAILABLE)));

        searchVehiclesUseCase.search(criteria);

        verify(reservationOccupancyPort, never()).isOccupied(anyInt(), any(), any());
    }

    @Test
    void emptyCatalogIsAnEmptyPageNotAnError() {
        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder().build();
        when(vehicleRepositoryPort.searchAvailable(criteria)).thenReturn(List.of());

        PagedResult<Vehicle> result = searchVehiclesUseCase.search(criteria);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getPage()).isEqualTo(1);
    }

    @Test
    void pagesInMemory() {
        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder().page(2).limit(2).build();
        when(vehicleRepositoryPort.searchAvailable(criteria)).thenReturn(List.of(
                TestData.vehicle(1, VehicleStatus.AVAILABLE), TestData.vehicle(2, VehicleStatus.AVAILABLE),
                TestData.vehicle(3, VehicleStatus.AVAILABLE)));

        PagedResult<Vehicle> result = searchVehiclesUseCase.search(criteria);

        assertThat(result.getItems()).extracting(Vehicle::getId).containsExactly(3);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void dateFromWithoutDateToIsInvalid() {
        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder().dateFrom(FROM).build();

        assertThatThrownBy(() -> searchVehiclesUseCase.search(criteria))
                .isInstanceOf(InvalidDataException.class)
                .hasFieldOrPropertyWithValue("field", "dateTo");
        verify(vehicleRepositoryPort, never()).searchAvailable(eq(criteria));
    }
}
