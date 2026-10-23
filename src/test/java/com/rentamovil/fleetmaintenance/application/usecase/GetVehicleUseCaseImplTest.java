package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceNotFoundException;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetVehicleUseCaseImplTest {

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;

    @InjectMocks
    private GetVehicleUseCaseImpl getVehicleUseCase;

    @Test
    void returnsTheVehicleWhenItExists() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));

        assertThat(getVehicleUseCase.getVehicle(5).getId()).isEqualTo(5);
    }

    @Test
    void unknownVehicleIsNotFound() {
        when(vehicleRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getVehicleUseCase.getVehicle(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
