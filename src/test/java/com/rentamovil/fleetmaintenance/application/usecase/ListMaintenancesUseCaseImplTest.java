package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.entity.Permissions;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ListMaintenancesUseCaseImplTest {

    @Mock
    private VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;

    @InjectMocks
    private ListMaintenancesUseCaseImpl listMaintenancesUseCase;

    @Test
    void anAdminSeesTheFilteredHistory() {
        AuthenticatedUser admin = new AuthenticatedUser(1, "ADMIN", List.of(Permissions.MAINTENANCE_UPDATE));
        VehicleMaintenance record = VehicleMaintenance.builder().id(7).vehicleId(3).status(MaintenanceStatus.IN_PROGRESS).build();
        when(vehicleMaintenanceRepositoryPort.search(3, MaintenanceStatus.IN_PROGRESS)).thenReturn(List.of(record));

        assertThat(listMaintenancesUseCase.list(admin, 3, MaintenanceStatus.IN_PROGRESS)).containsExactly(record);
    }

    @Test
    void aClientCannotReadTheHistory() {
        AuthenticatedUser client = new AuthenticatedUser(2, "CLIENT", List.of("vehicles:read"));

        assertThatThrownBy(() -> listMaintenancesUseCase.list(client, null, null))
                .isInstanceOf(ForbiddenException.class);
        verify(vehicleMaintenanceRepositoryPort, never()).search(any(), any());
    }
}
