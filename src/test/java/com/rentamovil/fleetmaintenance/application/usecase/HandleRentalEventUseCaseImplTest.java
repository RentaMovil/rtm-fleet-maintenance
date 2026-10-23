package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.common.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class HandleRentalEventUseCaseImplTest {

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private AuditPort auditPort;

    @InjectMocks
    private HandleRentalEventUseCaseImpl handleRentalEventUseCase;

    @Test
    void rentalStartedMovesVehicleToRented() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));

        handleRentalEventUseCase.rentalStarted(5);

        ArgumentCaptor<Vehicle> saved = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(VehicleStatus.RENTED);
        verify(auditPort).record(isNull(), eq("VEHICLE_RENTED"), eq("vehicle"), eq(5), any());
    }

    @Test
    void sameEventTwiceDoesNothingTheSecondTime() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.RENTED)));

        handleRentalEventUseCase.rentalStarted(5);

        verify(vehicleRepositoryPort, never()).save(any());
        verify(auditPort, never()).record(any(), any(), any(), anyInt(), any());
    }

    @Test
    void rentalCompletedMovesBackToAvailable() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.RENTED)));

        handleRentalEventUseCase.rentalCompleted(5);

        ArgumentCaptor<Vehicle> saved = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void rentalStartedOnVehicleUnderMaintenanceIsRejected() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.MAINTENANCE)));

        assertThatThrownBy(() -> handleRentalEventUseCase.rentalStarted(5))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_STATUS_TRANSITION");
    }
}
