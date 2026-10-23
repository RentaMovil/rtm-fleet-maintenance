package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetVehicleStatsUseCaseImplTest {

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;

    @InjectMocks
    private GetVehicleStatsUseCaseImpl getVehicleStatsUseCase;

    @Test
    void everyStatusIsPresentEvenWithoutVehicles() {
        when(vehicleRepositoryPort.countByStatus()).thenReturn(Map.of(VehicleStatus.AVAILABLE, 3L));

        Map<VehicleStatus, Long> stats = getVehicleStatsUseCase.getStats(TestData.ADMIN);

        assertThat(stats)
                .containsEntry(VehicleStatus.AVAILABLE, 3L)
                .containsEntry(VehicleStatus.RENTED, 0L)
                .containsEntry(VehicleStatus.MAINTENANCE, 0L)
                .containsEntry(VehicleStatus.RETIRED, 0L);
    }

    @Test
    void allZeroWhenRepositoryReturnsNothing() {
        when(vehicleRepositoryPort.countByStatus()).thenReturn(Map.of());

        Map<VehicleStatus, Long> stats = getVehicleStatsUseCase.getStats(TestData.ADMIN);

        assertThat(stats).containsValues(0L, 0L, 0L, 0L);
    }

    @Test
    void clientWithoutVehiclesUpdateIsForbidden() {
        assertThatThrownBy(() -> getVehicleStatsUseCase.getStats(TestData.CLIENT))
                .isInstanceOf(ForbiddenException.class);
        verify(vehicleRepositoryPort, never()).countByStatus();
    }
}
