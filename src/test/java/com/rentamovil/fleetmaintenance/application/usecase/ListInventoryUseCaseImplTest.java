package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ListInventoryUseCaseImplTest {

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;

    @InjectMocks
    private ListInventoryUseCaseImpl listInventoryUseCase;

    @Test
    void adminGetsTheInventoryFilteredByStatusAndBranch() {
        var vehicle = TestData.vehicle(5, VehicleStatus.MAINTENANCE);
        when(vehicleRepositoryPort.findInventory(VehicleStatus.MAINTENANCE, 4)).thenReturn(List.of(vehicle));

        List<Vehicle> result = listInventoryUseCase.list(TestData.ADMIN, VehicleStatus.MAINTENANCE, 4);

        assertThat(result).containsExactly(vehicle);
    }

    @Test
    void nullFiltersAreJustPassedThrough() {
        when(vehicleRepositoryPort.findInventory(null, null)).thenReturn(List.of());

        assertThat(listInventoryUseCase.list(TestData.ADMIN, null, null)).isEmpty();
    }

    @Test
    void clientWithoutVehiclesUpdateIsForbidden() {
        assertThatThrownBy(() -> listInventoryUseCase.list(TestData.CLIENT, null, null))
                .isInstanceOf(ForbiddenException.class);
        verify(vehicleRepositoryPort, never()).findInventory(null, null);
    }
}
