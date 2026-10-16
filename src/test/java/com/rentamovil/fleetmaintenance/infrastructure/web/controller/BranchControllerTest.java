package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.port.in.BranchData;
import com.rentamovil.fleetmaintenance.domain.port.in.ListBranchesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageBranchUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.BranchWriteRequest;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.OperatingHourDto;

/** /branches: listing is public, create/update/delete require branches:manage (checked in the use case). */
class BranchControllerTest {

    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(1, "ADMIN",
            List.of("branches:manage"));

    private final ListBranchesUseCase listBranchesUseCase = mock(ListBranchesUseCase.class);
    private final ManageBranchUseCase manageBranchUseCase = mock(ManageBranchUseCase.class);
    private final BranchController controller = new BranchController(listBranchesUseCase, manageBranchUseCase);

    // The 7 days are required by BranchWriteRequest; only their presence matters to the controller
    private static BranchWriteRequest request() {
        var request = new BranchWriteRequest();
        request.setName("Centro");
        request.setAddress("Cra 7 # 10-20");
        request.setCity("Bogota");
        request.setPhone("3001234567");
        request.setOperatingHours(List.of(1, 2, 3, 4, 5, 6, 7).stream()
                .map(day -> new OperatingHourDto(day, null, null, true))
                .toList());
        return request;
    }

    @Test
    void listBranchesIsMappedFromTheUseCase() {
        Branch centro = Branch.builder().id(1).name("Centro").build();
        when(listBranchesUseCase.listBranches()).thenReturn(List.of(centro));

        assertThat(controller.listBranches()).extracting(r -> r.name()).containsExactly("Centro");
    }

    @Test
    void createDelegatesToTheUseCase() {
        Branch saved = Branch.builder().id(9).name("Centro").build();
        when(manageBranchUseCase.create(eq(ADMIN), any(BranchData.class))).thenReturn(saved);

        var response = controller.create(ADMIN, request());

        assertThat(response.id()).isEqualTo(9);
        assertThat(response.name()).isEqualTo("Centro");
    }

    @Test
    void updateDelegatesToTheUseCaseWithTheGivenId() {
        Branch saved = Branch.builder().id(9).name("Centro Renovado").build();
        when(manageBranchUseCase.update(eq(ADMIN), eq(9), any(BranchData.class))).thenReturn(saved);

        var response = controller.update(ADMIN, 9, request());

        assertThat(response.name()).isEqualTo("Centro Renovado");
    }

    @Test
    void deleteDelegatesToTheUseCase() {
        controller.delete(ADMIN, 9);

        verify(manageBranchUseCase).delete(ADMIN, 9);
    }
}
