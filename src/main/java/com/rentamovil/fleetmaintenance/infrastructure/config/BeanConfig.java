package com.rentamovil.fleetmaintenance.infrastructure.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rentamovil.fleetmaintenance.application.usecase.ChangeVehicleStatusUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.CheckVehicleAvailabilityUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.CompleteMaintenanceUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.GetVehicleStatsUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.GetVehicleUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.HandleRentalEventUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ListBranchesUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ListMaintenancesUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ListReferenceDataUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ManageBranchUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.RegisterVehicleUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.SearchVehiclesUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.UpdateVehicleUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.VehicleDataResolver;
import com.rentamovil.fleetmaintenance.domain.port.in.ChangeVehicleStatusUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.CheckVehicleAvailabilityUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.CompleteMaintenanceUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.GetVehicleStatsUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.GetVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.HandleRentalEventUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ListBranchesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ListMaintenancesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ListReferenceDataUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageBranchUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.RegisterVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.SearchVehiclesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.UpdateVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.EventPublisherPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReservationOccupancyPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleMaintenanceRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

// Wires the use cases to their adapters.
// Use cases have no @Service so the domain doesn't depend on Spring.
@Configuration
public class BeanConfig {

    // Dates in UTC (modeling-conventions.md). Injected so tests can pin "today".
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public VehicleDataResolver vehicleDataResolver(ReferenceDataRepositoryPort referenceDataRepositoryPort,
                                                   BranchRepositoryPort branchRepositoryPort) {
        return new VehicleDataResolver(referenceDataRepositoryPort, branchRepositoryPort);
    }

    @Bean
    public SearchVehiclesUseCase searchVehiclesUseCase(VehicleRepositoryPort vehicleRepositoryPort,
                                                       ReservationOccupancyPort reservationOccupancyPort) {
        return new SearchVehiclesUseCaseImpl(vehicleRepositoryPort, reservationOccupancyPort);
    }

    @Bean
    public GetVehicleUseCase getVehicleUseCase(VehicleRepositoryPort vehicleRepositoryPort) {
        return new GetVehicleUseCaseImpl(vehicleRepositoryPort);
    }

    @Bean
    public CheckVehicleAvailabilityUseCase checkVehicleAvailabilityUseCase(VehicleRepositoryPort vehicleRepositoryPort,
                                                                           ReservationOccupancyPort reservationOccupancyPort) {
        return new CheckVehicleAvailabilityUseCaseImpl(vehicleRepositoryPort, reservationOccupancyPort);
    }

    @Bean
    public RegisterVehicleUseCase registerVehicleUseCase(VehicleRepositoryPort vehicleRepositoryPort,
                                                         VehicleDataResolver vehicleDataResolver,
                                                         AuditPort auditPort) {
        return new RegisterVehicleUseCaseImpl(vehicleRepositoryPort, vehicleDataResolver, auditPort);
    }

    @Bean
    public UpdateVehicleUseCase updateVehicleUseCase(VehicleRepositoryPort vehicleRepositoryPort,
                                                     VehicleDataResolver vehicleDataResolver,
                                                     AuditPort auditPort) {
        return new UpdateVehicleUseCaseImpl(vehicleRepositoryPort, vehicleDataResolver, auditPort);
    }

    @Bean
    public ChangeVehicleStatusUseCase changeVehicleStatusUseCase(
            VehicleRepositoryPort vehicleRepositoryPort,
            VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort,
            ReferenceDataRepositoryPort referenceDataRepositoryPort,
            ReservationOccupancyPort reservationOccupancyPort,
            EventPublisherPort eventPublisherPort,
            AuditPort auditPort,
            Clock clock,
            @Value("${app.fleet.retire-reservation-horizon-days}") long retireReservationHorizonDays) {
        return new ChangeVehicleStatusUseCaseImpl(vehicleRepositoryPort, vehicleMaintenanceRepositoryPort,
                referenceDataRepositoryPort, reservationOccupancyPort, eventPublisherPort, auditPort, clock,
                retireReservationHorizonDays);
    }

    @Bean
    public CompleteMaintenanceUseCase completeMaintenanceUseCase(
            VehicleRepositoryPort vehicleRepositoryPort,
            VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort,
            EventPublisherPort eventPublisherPort,
            AuditPort auditPort,
            Clock clock) {
        return new CompleteMaintenanceUseCaseImpl(vehicleRepositoryPort, vehicleMaintenanceRepositoryPort,
                eventPublisherPort, auditPort, clock);
    }

    @Bean
    public GetVehicleStatsUseCase getVehicleStatsUseCase(VehicleRepositoryPort vehicleRepositoryPort) {
        return new GetVehicleStatsUseCaseImpl(vehicleRepositoryPort);
    }

    @Bean
    public ListMaintenancesUseCase listMaintenancesUseCase(VehicleMaintenanceRepositoryPort vehicleMaintenanceRepositoryPort) {
        return new ListMaintenancesUseCaseImpl(vehicleMaintenanceRepositoryPort);
    }

    @Bean
    public ListBranchesUseCase listBranchesUseCase(BranchRepositoryPort branchRepositoryPort) {
        return new ListBranchesUseCaseImpl(branchRepositoryPort);
    }

    @Bean
    public ManageBranchUseCase manageBranchUseCase(BranchRepositoryPort branchRepositoryPort,
                                                   VehicleRepositoryPort vehicleRepositoryPort,
                                                   AuditPort auditPort) {
        return new ManageBranchUseCaseImpl(branchRepositoryPort, vehicleRepositoryPort, auditPort);
    }

    @Bean
    public ListReferenceDataUseCase listReferenceDataUseCase(ReferenceDataRepositoryPort referenceDataRepositoryPort) {
        return new ListReferenceDataUseCaseImpl(referenceDataRepositoryPort);
    }

    @Bean
    public HandleRentalEventUseCase handleRentalEventUseCase(VehicleRepositoryPort vehicleRepositoryPort,
                                                             AuditPort auditPort) {
        return new HandleRentalEventUseCaseImpl(vehicleRepositoryPort, auditPort);
    }
}
