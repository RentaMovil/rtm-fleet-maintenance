package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.fleetmaintenance.domain.port.in.VehicleData;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class RegisterVehicleUseCaseImplTest {

    private static final VehicleData DATA = new VehicleData(" abc 123", 7, 5, 2024,
            new BigDecimal("150000"), 4, null, null);

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private ReferenceDataRepositoryPort referenceDataRepositoryPort;
    @Mock
    private BranchRepositoryPort branchRepositoryPort;
    @Mock
    private AuditPort auditPort;

    private RegisterVehicleUseCaseImpl registerVehicleUseCase;

    @BeforeEach
    void setUp() {
        registerVehicleUseCase = new RegisterVehicleUseCaseImpl(vehicleRepositoryPort,
                new VehicleDataResolver(referenceDataRepositoryPort, branchRepositoryPort), auditPort);
    }

    @Test
    void registersVehicleAlwaysAvailableWithNormalizedPlate() {
        when(vehicleRepositoryPort.existsByPlate("ABC123")).thenReturn(false);
        when(referenceDataRepositoryPort.findVehicleModelById(7)).thenReturn(Optional.of(TestData.COROLLA));
        when(branchRepositoryPort.existsById(4)).thenReturn(true);
        when(vehicleRepositoryPort.save(any())).thenAnswer(inv -> {
            Vehicle vehicle = inv.getArgument(0);
            vehicle.setId(10);
            return vehicle;
        });

        Vehicle result = registerVehicleUseCase.register(TestData.ADMIN, DATA);

        ArgumentCaptor<Vehicle> saved = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(saved.getValue().getPlate()).isEqualTo("ABC123");
        assertThat(saved.getValue().getMileage()).isEqualByComparingTo("0");
        assertThat(result.getId()).isEqualTo(10);
        verify(auditPort).record(eq(1), eq("VEHICLE_REGISTERED"), eq("vehicle"), eq(10), any());
    }

    @Test
    void rejectsRepeatedPlate() {
        when(vehicleRepositoryPort.existsByPlate("ABC123")).thenReturn(true);

        assertThatThrownBy(() -> registerVehicleUseCase.register(TestData.ADMIN, DATA))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasFieldOrPropertyWithValue("code", "PLATE_ALREADY_EXISTS");
        verify(vehicleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsUnknownModel() {
        when(referenceDataRepositoryPort.findVehicleModelById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerVehicleUseCase.register(TestData.ADMIN, DATA))
                .isInstanceOf(InvalidDataException.class)
                .hasFieldOrPropertyWithValue("field", "modelId");
    }

    @Test
    void clientCannotRegisterVehicles() {
        assertThatThrownBy(() -> registerVehicleUseCase.register(TestData.CLIENT, DATA))
                .isInstanceOf(ForbiddenException.class);
        verify(auditPort, never()).record(any(), any(), any(), anyInt(), any());
    }
}
