package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.DomainEvent;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CompleteMaintenanceUseCaseImplTest {

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    @Mock
    private EventPublisherPort eventPublisherPort;
    @Mock
    private AuditPort auditPort;

    private CompleteMaintenanceUseCaseImpl completeMaintenanceUseCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-02T15:00:00Z"), ZoneOffset.UTC);
        completeMaintenanceUseCase = new CompleteMaintenanceUseCaseImpl(vehicleRepositoryPort,
                vehicleMaintenanceRepositoryPort, eventPublisherPort, auditPort, clock);
    }

    @Test
    void closesMaintenanceAndMovesBackToAvailable() {
        VehicleMaintenance inProgress = VehicleMaintenance.builder().id(30).vehicleId(5)
                .status(MaintenanceStatus.IN_PROGRESS).startDate(LocalDate.of(2026, 9, 28)).build();
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.MAINTENANCE)));
        when(vehicleMaintenanceRepositoryPort.findInProgressByVehicleId(5)).thenReturn(Optional.of(inProgress));
        when(vehicleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = completeMaintenanceUseCase.complete(TestData.ADMIN, 5, new BigDecimal("250000"));

        assertThat(result.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(inProgress.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(inProgress.getEndDate()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(inProgress.getCost()).isEqualByComparingTo("250000");

        ArgumentCaptor<DomainEvent> event = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisherPort).publish(event.capture());
        assertThat(event.getValue().getEventType()).isEqualTo("MaintenanceCompleted");
    }

    @Test
    void noMaintenanceInProgressIsConflict() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));
        when(vehicleMaintenanceRepositoryPort.findInProgressByVehicleId(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completeMaintenanceUseCase.complete(TestData.ADMIN, 5, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "NO_MAINTENANCE_IN_PROGRESS");
        verify(vehicleRepositoryPort, never()).save(any());
    }
}
