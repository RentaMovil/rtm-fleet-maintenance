package com.rentamovil.fleetmaintenance.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rentamovil.fleetmaintenance.application.usecase.ListBranchesUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ListReferenceDataUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ManageBranchUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.RegisterVehicleUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.UpdateVehicleUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.VehicleDataResolver;
import com.rentamovil.fleetmaintenance.domain.port.in.ListBranchesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ListReferenceDataUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageBranchUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.RegisterVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.UpdateVehicleUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.VehicleRepositoryPort;

// Wires the use cases to their adapters.
// Use cases have no @Service so the domain doesn't depend on Spring.
@Configuration
public class BeanConfig {

    @Bean
    public VehicleDataResolver vehicleDataResolver(ReferenceDataRepositoryPort referenceDataRepositoryPort,
                                                   BranchRepositoryPort branchRepositoryPort) {
        return new VehicleDataResolver(referenceDataRepositoryPort, branchRepositoryPort);
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
}
