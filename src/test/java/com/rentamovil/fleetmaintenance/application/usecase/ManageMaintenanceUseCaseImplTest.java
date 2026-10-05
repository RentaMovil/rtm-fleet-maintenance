package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.rentamovil.fleetmaintenance.domain.entity.DomainEvent;
import com.rentamovil.fleetmaintenance.domain.entity.Vehicle;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleMaintenance;
import com.rentamovil.fleetmaintenance.domain.enums.MaintenanceStatus;
import com.rentamovil.fleetmaintenance.domain.enums.VehicleStatus;
import com.rentamovil.fleetmaintenance.domain.exception.BusinessRuleException;
import com.rentamovil.fleetmaintenance.domain.exception.ForbiddenException;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidDataException;
import com.rentamovil.fleetmaintenance.domain.port.in.MaintenanceData;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

// Shared stubs (in-memory maintenance store) aren't used by every test, hence LENIENT
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManageMaintenanceUseCaseImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final MaintenanceData OIL_CHANGE = new MaintenanceData(1, null, null, "check filter", null);

    @Mock
    private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock
    private VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort;
    @Mock
    private ReferenceDataRepositoryPort referenceDataRepositoryPort;
    @Mock
    private EventPublisherPort eventPublisherPort;
    @Mock
    private AuditPort auditPort;

    private ManageMaintenanceUseCaseImpl useCase;
    // What the repository "stores": save assigns an id, findById reads it back
    private final Map<Integer, VehicleMaintenance> store = new HashMap<>();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-05T15:00:00Z"), ZoneOffset.UTC);
        useCase = new ManageMaintenanceUseCaseImpl(vehicleRepositoryPort, vehicleMaintenanceRepositoryPort,
                referenceDataRepositoryPort,
                new MaintenanceLifecycle(vehicleRepositoryPort, vehicleMaintenanceRepositoryPort, eventPublisherPort,
                        auditPort, clock),
                auditPort);
        when(referenceDataRepositoryPort.existsMaintenanceType(1)).thenReturn(true);
        when(vehicleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleMaintenanceRepositoryPort.save(any())).thenAnswer(inv -> {
            VehicleMaintenance maintenance = inv.getArgument(0);
            if (maintenance.getId() == null) {
                maintenance.setId(40 + store.size());
            }
            store.put(maintenance.getId(), maintenance);
            return maintenance;
        });
        when(vehicleMaintenanceRepositoryPort.findById(anyInt()))
                .thenAnswer(inv -> Optional.ofNullable(store.get(inv.<Integer>getArgument(0))));
    }

    private Vehicle vehicle(VehicleStatus status) {
        Vehicle vehicle = TestData.vehicle(5, status);
        when(vehicleRepositoryPort.findById(5)).thenReturn(Optional.of(vehicle));
        return vehicle;
    }

    private VehicleMaintenance stored(MaintenanceStatus status) {
        VehicleMaintenance maintenance = VehicleMaintenance.builder().id(30).vehicleId(5).maintenanceTypeId(1)
                .startDate(TODAY).status(status).build();
        store.put(30, maintenance);
        return maintenance;
    }

    @Test
    void schedulingDoesNotTouchTheVehicle() {
        Vehicle vehicle = vehicle(VehicleStatus.RENTED);
        MaintenanceData data = new MaintenanceData(1, TODAY.plusDays(10), null, null, null);

        VehicleMaintenance result = useCase.create(TestData.ADMIN, 5, MaintenanceStatus.SCHEDULED, data);

        assertThat(result.getStatus()).isEqualTo(MaintenanceStatus.SCHEDULED);
        assertThat(result.getStartDate()).isEqualTo(TODAY.plusDays(10));
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RENTED);
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    void scheduledInThePastIsInvalid() {
        vehicle(VehicleStatus.AVAILABLE);
        MaintenanceData data = new MaintenanceData(1, TODAY.minusDays(1), null, null, null);

        assertThatThrownBy(() -> useCase.create(TestData.ADMIN, 5, MaintenanceStatus.SCHEDULED, data))
                .isInstanceOf(InvalidDataException.class)
                .hasFieldOrPropertyWithValue("field", "startDate");
    }

    @Test
    void startingNowSendsTheVehicleToMaintenance() {
        Vehicle vehicle = vehicle(VehicleStatus.AVAILABLE);

        VehicleMaintenance result = useCase.create(TestData.ADMIN, 5, MaintenanceStatus.IN_PROGRESS, OIL_CHANGE);

        assertThat(result.getStatus()).isEqualTo(MaintenanceStatus.IN_PROGRESS);
        assertThat(result.getStartDate()).isEqualTo(TODAY);
        assertThat(result.getObservations()).isEqualTo("check filter");
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
        verify(eventPublisherPort).publish(any(DomainEvent.class));
    }

    @Test
    void cannotStartOnARentedVehicle() {
        vehicle(VehicleStatus.RENTED);

        assertThatThrownBy(() -> useCase.create(TestData.ADMIN, 5, MaintenanceStatus.IN_PROGRESS, OIL_CHANGE))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "VEHICLE_NOT_AVAILABLE");
        verify(vehicleMaintenanceRepositoryPort, never()).save(any());
    }

    @Test
    void retiredVehicleGetsNoMaintenance() {
        vehicle(VehicleStatus.RETIRED);
        MaintenanceData data = new MaintenanceData(1, TODAY, null, null, null);

        assertThatThrownBy(() -> useCase.create(TestData.ADMIN, 5, MaintenanceStatus.SCHEDULED, data))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "VEHICLE_RETIRED");
    }

    @Test
    void editsOnlyTheFieldsSentAndEmptyClears() {
        VehicleMaintenance maintenance = stored(MaintenanceStatus.SCHEDULED);
        maintenance.setObservations("old note");
        maintenance.setCost(new BigDecimal("100"));

        useCase.update(TestData.ADMIN, 30, new MaintenanceData(null, null, new BigDecimal("250"), "", null));

        assertThat(maintenance.getCost()).isEqualByComparingTo("250");
        assertThat(maintenance.getObservations()).isNull();
        assertThat(maintenance.getStartDate()).isEqualTo(TODAY);
    }

    @Test
    void closedMaintenanceIsReadOnly() {
        stored(MaintenanceStatus.COMPLETED);

        assertThatThrownBy(() -> useCase.update(TestData.ADMIN, 30, new MaintenanceData(null, null, BigDecimal.ONE, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "MAINTENANCE_CLOSED");
        verify(vehicleMaintenanceRepositoryPort, never()).save(any());
    }

    @Test
    void startingAScheduledOneMovesTheVehicleAndResetsTheDate() {
        Vehicle vehicle = vehicle(VehicleStatus.AVAILABLE);
        VehicleMaintenance maintenance = stored(MaintenanceStatus.SCHEDULED);
        maintenance.setStartDate(TODAY.plusDays(3));

        useCase.changeStatus(TestData.ADMIN, 30, MaintenanceStatus.IN_PROGRESS, null);

        assertThat(maintenance.getStatus()).isEqualTo(MaintenanceStatus.IN_PROGRESS);
        assertThat(maintenance.getStartDate()).isEqualTo(TODAY);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
    }

    @Test
    void completingReleasesTheVehicleAndStoresTheCost() {
        Vehicle vehicle = vehicle(VehicleStatus.MAINTENANCE);
        VehicleMaintenance maintenance = stored(MaintenanceStatus.IN_PROGRESS);

        useCase.changeStatus(TestData.ADMIN, 30, MaintenanceStatus.COMPLETED, new BigDecimal("300000"));

        assertThat(maintenance.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(maintenance.getEndDate()).isEqualTo(TODAY);
        assertThat(maintenance.getCost()).isEqualByComparingTo("300000");
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void cancellingInProgressReleasesTheVehicleWithoutEvent() {
        Vehicle vehicle = vehicle(VehicleStatus.MAINTENANCE);
        VehicleMaintenance maintenance = stored(MaintenanceStatus.IN_PROGRESS);

        useCase.changeStatus(TestData.ADMIN, 30, MaintenanceStatus.CANCELLED, null);

        assertThat(maintenance.getStatus()).isEqualTo(MaintenanceStatus.CANCELLED);
        assertThat(maintenance.getEndDate()).isEqualTo(TODAY);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    void cancellingScheduledDoesNotTouchTheVehicle() {
        Vehicle vehicle = vehicle(VehicleStatus.RENTED);
        stored(MaintenanceStatus.SCHEDULED);

        useCase.changeStatus(TestData.ADMIN, 30, MaintenanceStatus.CANCELLED, null);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RENTED);
        verify(vehicleRepositoryPort, never()).save(any());
    }

    @Test
    void nothingLeavesAClosedMaintenance() {
        vehicle(VehicleStatus.AVAILABLE);
        stored(MaintenanceStatus.CANCELLED);

        assertThatThrownBy(() -> useCase.changeStatus(TestData.ADMIN, 30, MaintenanceStatus.IN_PROGRESS, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_STATUS_TRANSITION");
    }

    @Test
    void clientCannotManageMaintenances() {
        assertThatThrownBy(() -> useCase.create(TestData.CLIENT, 5, MaintenanceStatus.SCHEDULED, OIL_CHANGE))
                .isInstanceOf(ForbiddenException.class);
    }
}
