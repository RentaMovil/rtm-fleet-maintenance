package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.entity.OperatingHour;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.in.BranchData;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ManageBranchUseCaseImplTest {

    @Mock
    private BranchRepositoryPort branchRepositoryPort;
    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private AuditPort auditPort;

    @InjectMocks
    private ManageBranchUseCaseImpl manageBranchUseCase;

    // Monday to Saturday 08:00-18:00, Sunday closed (deliberately out of order)
    private static List<OperatingHour> week() {
        List<OperatingHour> week = new ArrayList<>();
        week.add(new OperatingHour(7, null, null, true));
        for (int day = 1; day <= 6; day++) {
            week.add(new OperatingHour(day, LocalTime.of(8, 0), LocalTime.of(18, 0), false));
        }
        return week;
    }

    private static BranchData data(List<OperatingHour> hours) {
        return new BranchData(" Centro ", "10th Street # 5-20", "Bogota", "6011234567", null, null, hours);
    }

    @Test
    void storesBothCoordinates() {
        when(branchRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Branch result = manageBranchUseCase.create(TestData.ADMIN, new BranchData("Centro", "a", "Neiva", "1",
                new BigDecimal("2.930321"), new BigDecimal("-75.266170"), week()));

        assertThat(result.getLatitude()).isEqualByComparingTo("2.930321");
        assertThat(result.getLongitude()).isEqualByComparingTo("-75.266170");
    }

    @Test
    void halfALocationIsInvalid() {
        BranchData onlyLatitude = new BranchData("Centro", "a", "Neiva", "1", new BigDecimal("2.93"), null, week());

        assertThatThrownBy(() -> manageBranchUseCase.create(TestData.ADMIN, onlyLatitude))
                .isInstanceOf(InvalidDataException.class)
                .hasFieldOrPropertyWithValue("field", "longitude");
        verify(branchRepositoryPort, never()).save(any());
    }

    @Test
    void createsBranchWithSortedWeek() {
        when(branchRepositoryPort.save(any())).thenAnswer(inv -> {
            Branch branch = inv.getArgument(0);
            branch.setId(4);
            return branch;
        });

        Branch result = manageBranchUseCase.create(TestData.ADMIN, data(week()));

        assertThat(result.getName()).isEqualTo("Centro");
        assertThat(result.getOperatingHours()).extracting(OperatingHour::getDayOfWeek).containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(result.getOperatingHours().get(6).isClosed()).isTrue();
    }

    @Test
    void repeatedDayIsInvalid() {
        List<OperatingHour> hours = week();
        hours.set(0, new OperatingHour(1, LocalTime.of(9, 0), LocalTime.of(12, 0), false));

        assertThatThrownBy(() -> manageBranchUseCase.create(TestData.ADMIN, data(hours)))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("repeated");
        verify(branchRepositoryPort, never()).save(any());
    }

    @Test
    void mustOpenBeforeClosing() {
        List<OperatingHour> hours = week();
        hours.set(1, new OperatingHour(1, LocalTime.of(18, 0), LocalTime.of(8, 0), false));

        assertThatThrownBy(() -> manageBranchUseCase.create(TestData.ADMIN, data(hours)))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("must open before it closes");
    }

    @Test
    void branchWithVehiclesIsNotDeleted() {
        when(branchRepositoryPort.findById(4)).thenReturn(Optional.of(Branch.builder().id(4).name("Centro").build()));
        when(vehicleRepositoryPort.existsByBranchId(4)).thenReturn(true);

        assertThatThrownBy(() -> manageBranchUseCase.delete(TestData.ADMIN, 4))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "BRANCH_HAS_VEHICLES");
        verify(branchRepositoryPort, never()).deleteById(anyInt());
    }

    @Test
    void deletesBranchWithoutVehicles() {
        when(branchRepositoryPort.findById(4)).thenReturn(Optional.of(Branch.builder().id(4).name("Centro").build()));
        when(vehicleRepositoryPort.existsByBranchId(4)).thenReturn(false);

        manageBranchUseCase.delete(TestData.ADMIN, 4);

        verify(branchRepositoryPort).deleteById(4);
        ArgumentCaptor<String> action = ArgumentCaptor.forClass(String.class);
        verify(auditPort).record(any(), action.capture(), any(), any(), any());
        assertThat(action.getValue()).isEqualTo("BRANCH_DELETED");
    }

    @Test
    void clientCannotManageBranches() {
        assertThatThrownBy(() -> manageBranchUseCase.delete(TestData.CLIENT, 4))
                .isInstanceOf(ForbiddenException.class);
    }
}
