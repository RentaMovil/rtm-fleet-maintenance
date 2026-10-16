package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.MaintenanceVehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.port.in.ListMaintenancesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageMaintenanceUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.MaintenanceData;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceCreateRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceStatusRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.MaintenanceUpdateRequest;

/** /maintenances: create requires maintenance:create, editing and status changes maintenance:update. */
class MaintenanceControllerTest {

    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(1, "ADMIN",
            List.of("maintenance:create", "maintenance:update"));

    private final ListMaintenancesUseCase listMaintenancesUseCase = mock(ListMaintenancesUseCase.class);
    private final ManageMaintenanceUseCase manageMaintenanceUseCase = mock(ManageMaintenanceUseCase.class);
    private final MaintenanceController controller =
            new MaintenanceController(listMaintenancesUseCase, manageMaintenanceUseCase);

    private static VehicleMaintenance maintenance(Integer id, MaintenanceStatus status) {
        return VehicleMaintenance.builder()
                .id(id)
                .status(status)
                .vehicle(new MaintenanceVehicle(5, "ABC123", "Toyota", "Corolla"))
                .maintenanceType(new CatalogItem(2, "Oil change"))
                .build();
    }

    @Test
    void listDelegatesTheFiltersAndMapsTheResult() {
        when(listMaintenancesUseCase.list(ADMIN, 5, MaintenanceStatus.SCHEDULED))
                .thenReturn(List.of(maintenance(1, MaintenanceStatus.SCHEDULED)));

        var response = controller.list(ADMIN, 5, MaintenanceStatus.SCHEDULED);

        assertThat(response).extracting(r -> r.status()).containsExactly("SCHEDULED");
    }

    @Test
    void createDelegatesWithTheParsedStatus() {
        when(manageMaintenanceUseCase.create(eq(ADMIN), eq(5), eq(MaintenanceStatus.SCHEDULED), any(MaintenanceData.class)))
                .thenReturn(maintenance(10, MaintenanceStatus.SCHEDULED));

        var request = new MaintenanceCreateRequest();
        request.setVehicleId(5);
        request.setMaintenanceTypeId(2);
        request.setStatus("SCHEDULED");

        var response = controller.create(ADMIN, request);

        assertThat(response.id()).isEqualTo(10);
        verify(manageMaintenanceUseCase).create(eq(ADMIN), eq(5), eq(MaintenanceStatus.SCHEDULED), any(MaintenanceData.class));
    }

    @Test
    void updateDelegatesToTheUseCaseWithTheGivenId() {
        when(manageMaintenanceUseCase.update(eq(ADMIN), eq(10), any(MaintenanceData.class)))
                .thenReturn(maintenance(10, MaintenanceStatus.SCHEDULED));

        var request = new MaintenanceUpdateRequest();
        request.setObservations("Cambio de llantas tambien");

        var response = controller.update(ADMIN, 10, request);

        assertThat(response.id()).isEqualTo(10);
    }

    @Test
    void changeStatusDelegatesTheParsedStatusAndCost() {
        when(manageMaintenanceUseCase.changeStatus(ADMIN, 10, MaintenanceStatus.COMPLETED, new BigDecimal("50000")))
                .thenReturn(maintenance(10, MaintenanceStatus.COMPLETED));

        var request = new MaintenanceStatusRequest();
        request.setStatus("COMPLETED");
        request.setCost(new BigDecimal("50000"));

        var response = controller.changeStatus(ADMIN, 10, request);

        assertThat(response.status()).isEqualTo("COMPLETED");
    }
}
