package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CheckVehicleAvailabilityUseCaseImplTest {

    private static final LocalDate FROM = LocalDate.of(2026, 11, 10);
    private static final LocalDate TO = LocalDate.of(2026, 11, 15);

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private ReservationOccupancyPort reservationOccupancyPort;

    @InjectMocks
    private CheckVehicleAvailabilityUseCaseImpl checkVehicleAvailabilityUseCase;

    @Test
    void availableWhenAvailableAndNoOverlappingReservations() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));
        when(reservationOccupancyPort.isOccupied(5, FROM, TO)).thenReturn(false);

        assertThat(checkVehicleAvailabilityUseCase.isAvailable(5, FROM, TO)).isTrue();
    }

    @Test
    void notAvailableWithOverlappingReservation() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));
        when(reservationOccupancyPort.isOccupied(5, FROM, TO)).thenReturn(true);

        assertThat(checkVehicleAvailabilityUseCase.isAvailable(5, FROM, TO)).isFalse();
    }

    @Test
    void underMaintenanceIsNotAvailableAndBookingIsNotAsked() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.MAINTENANCE)));

        assertThat(checkVehicleAvailabilityUseCase.isAvailable(5, FROM, TO)).isFalse();
        verify(reservationOccupancyPort, never()).isOccupied(anyInt(), any(), any());
    }

    @Test
    void unknownVehicleIsNotFound() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkVehicleAvailabilityUseCase.isAvailable(5, FROM, TO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rangeMustGoForward() {
        assertThatThrownBy(() -> checkVehicleAvailabilityUseCase.isAvailable(5, TO, FROM))
                .isInstanceOf(InvalidDataException.class);
    }
}
