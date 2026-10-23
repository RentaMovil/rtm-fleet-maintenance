package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
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
import com.rentamovil.common.exception.BusinessRuleException;
import com.rentamovil.common.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ChangeVehicleStatusUseCaseImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 2);

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    @Mock
    private ReferenceDataRepositoryPort referenceDataRepositoryPort;
    @Mock
    private ReservationOccupancyPort reservationOccupancyPort;
    @Mock
    private EventPublisherPort eventPublisherPort;
    @Mock
    private AuditPort auditPort;

    private ChangeVehicleStatusUseCaseImpl changeVehicleStatusUseCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-02T15:00:00Z"), ZoneOffset.UTC);
        changeVehicleStatusUseCase = new ChangeVehicleStatusUseCaseImpl(vehicleRepositoryPort,
                vehicleMaintenanceRepositoryPort, referenceDataRepositoryPort, reservationOccupancyPort,
                new MaintenanceLifecycle(vehicleRepositoryPort, vehicleMaintenanceRepositoryPort, eventPublisherPort,
                        auditPort, clock),
                auditPort, 730);
    }

    @Test
    void sendToMaintenanceCreatesRecordAndEmitsEvent() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));
        when(referenceDataRepositoryPort.existsMaintenanceType(2)).thenReturn(true);
        when(vehicleMaintenanceRepositoryPort.save(any())).thenAnswer(inv -> {
            VehicleMaintenance maintenance = inv.getArgument(0);
            maintenance.setId(30);
            return maintenance;
        });
        when(vehicleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = changeVehicleStatusUseCase.changeStatus(TestData.ADMIN, 5, VehicleStatus.MAINTENANCE, 2);

        assertThat(result.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
        ArgumentCaptor<VehicleMaintenance> maintenance = ArgumentCaptor.forClass(VehicleMaintenance.class);
        verify(vehicleMaintenanceRepositoryPort).save(maintenance.capture());
        assertThat(maintenance.getValue().getStatus()).isEqualTo(MaintenanceStatus.IN_PROGRESS);
        assertThat(maintenance.getValue().getStartDate()).isEqualTo(TODAY);

        ArgumentCaptor<DomainEvent> event = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisherPort).publish(event.capture());
        assertThat(event.getValue().getEventType()).isEqualTo("VehicleSentToMaintenance");
        assertThat(event.getValue().getPayload()).containsEntry("maintenanceId", 30).containsEntry("vehicleId", 5);
    }

    @Test
    void rentedVehicleCannotGoToMaintenance() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.RENTED)));

        assertThatThrownBy(() -> changeVehicleStatusUseCase.changeStatus(TestData.ADMIN, 5, VehicleStatus.MAINTENANCE, 2))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "VEHICLE_RENTED");
        verify(vehicleRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    void maintenanceWithoutTypeIsInvalid() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));

        assertThatThrownBy(() -> changeVehicleStatusUseCase.changeStatus(TestData.ADMIN, 5, VehicleStatus.MAINTENANCE, null))
                .isInstanceOf(InvalidDataException.class)
                .hasFieldOrPropertyWithValue("field", "maintenanceTypeId");
    }

    @Test
    void availableIsNotAManualTarget() {
        assertThatThrownBy(() -> changeVehicleStatusUseCase.changeStatus(TestData.ADMIN, 5, VehicleStatus.AVAILABLE, null))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void retiresVehicleWithoutFutureReservations() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));
        when(reservationOccupancyPort.isOccupied(5, TODAY, TODAY.plusDays(730))).thenReturn(false);
        when(vehicleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = changeVehicleStatusUseCase.changeStatus(TestData.ADMIN, 5, VehicleStatus.RETIRED, null);

        assertThat(result.getStatus()).isEqualTo(VehicleStatus.RETIRED);
        verify(auditPort).record(eq(1), eq("VEHICLE_RETIRED"), eq("vehicle"), eq(5), any());
    }

    @Test
    void cannotRetireWithFutureReservations() {
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.AVAILABLE)));
        when(reservationOccupancyPort.isOccupied(5, TODAY, TODAY.plusDays(730))).thenReturn(true);

        assertThatThrownBy(() -> changeVehicleStatusUseCase.changeStatus(TestData.ADMIN, 5, VehicleStatus.RETIRED, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "VEHICLE_HAS_FUTURE_RESERVATIONS");
        verify(vehicleRepositoryPort, never()).save(any());
    }

    @Test
    void retiringFromMaintenanceCancelsItWithoutEvent() {
        VehicleMaintenance inProgress = VehicleMaintenance.builder().id(30).vehicleId(5)
                .status(MaintenanceStatus.IN_PROGRESS).startDate(TODAY.minusDays(3)).build();
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(TestData.vehicle(5, VehicleStatus.MAINTENANCE)));
        when(vehicleMaintenanceRepositoryPort.findOpenByVehicleId(5)).thenReturn(List.of(inProgress));
        when(vehicleMaintenanceRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = changeVehicleStatusUseCase.changeStatus(TestData.ADMIN, 5, VehicleStatus.RETIRED, null);

        //VehicleMaintenance.INV-007: cancelled, not completed; and the vehicle stays RETIRED
        assertThat(inProgress.getStatus()).isEqualTo(MaintenanceStatus.CANCELLED);
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.RETIRED);
        assertThat(inProgress.getEndDate()).isEqualTo(TODAY);
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    void clientCannotSendToMaintenance() {
        assertThatThrownBy(() -> changeVehicleStatusUseCase.changeStatus(TestData.CLIENT, 5, VehicleStatus.MAINTENANCE, 2))
                .isInstanceOf(ForbiddenException.class);
    }
}
