package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.port.in.VehicleData;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UpdateVehicleUseCaseImplTest {

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private ReferenceDataRepositoryPort referenceDataRepositoryPort;
    @Mock
    private BranchRepositoryPort branchRepositoryPort;
    @Mock
    private AuditPort auditPort;

    private UpdateVehicleUseCaseImpl updateVehicleUseCase;

    @BeforeEach
    void setUp() {
        updateVehicleUseCase = new UpdateVehicleUseCaseImpl(vehicleRepositoryPort,
                new VehicleDataResolver(referenceDataRepositoryPort, branchRepositoryPort), auditPort);
    }

    private static VehicleData data(String mileage) {
        return new VehicleData("ABC125", 7, 5, 2024, new BigDecimal("180000"), 4,
                mileage == null ? null : new BigDecimal(mileage), null);
    }

    @Test
    void changesPriceWithoutTouchingStatus() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.MAINTENANCE)));
        when(referenceDataRepositoryPort.findVehicleModelById(7)).thenReturn(Optional.of(TestData.COROLLA));
        when(branchRepositoryPort.existsById(4)).thenReturn(true);
        when(vehicleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = updateVehicleUseCase.update(TestData.ADMIN, 5, data(null));

        assertThat(result.getDailyPrice()).isEqualByComparingTo("180000");
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
        //Without mileage in the request the current one is kept
        assertThat(result.getMileage()).isEqualByComparingTo("1000");
    }

    @Test
    void mileageNeverDecreases() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));

        assertThatThrownBy(() -> updateVehicleUseCase.update(TestData.ADMIN, 5, data("999")))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "MILEAGE_CANNOT_DECREASE");
        verify(vehicleRepositoryPort, never()).save(any());
    }

    @Test
    void retiredVehicleCannotBeEdited() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.RETIRED)));

        assertThatThrownBy(() -> updateVehicleUseCase.update(TestData.ADMIN, 5, data(null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "VEHICLE_RETIRED");
    }
}
