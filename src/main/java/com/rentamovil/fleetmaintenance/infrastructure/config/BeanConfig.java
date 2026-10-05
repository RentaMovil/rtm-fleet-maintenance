package com.rentamovil.fleetmaintenance.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rentamovil.fleetmaintenance.application.usecase.ListBranchesUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ListReferenceDataUseCaseImpl;
import com.rentamovil.fleetmaintenance.application.usecase.ManageBranchUseCaseImpl;
import com.rentamovil.fleetmaintenance.domain.port.in.ListBranchesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ListReferenceDataUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageBranchUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.AuditPort;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;

// Wires the use cases to their adapters.
// Use cases have no @Service so the domain doesn't depend on Spring.
@Configuration
public class BeanConfig {

    @Bean
    public ListBranchesUseCase listBranchesUseCase(BranchRepositoryPort branchRepositoryPort) {
        return new ListBranchesUseCaseImpl(branchRepositoryPort);
    }

    @Bean
    public ManageBranchUseCase manageBranchUseCase(BranchRepositoryPort branchRepositoryPort,
                                                   AuditPort auditPort) {
        return new ManageBranchUseCaseImpl(branchRepositoryPort, auditPort);
    }

    @Bean
    public ListReferenceDataUseCase listReferenceDataUseCase(ReferenceDataRepositoryPort referenceDataRepositoryPort) {
        return new ListReferenceDataUseCaseImpl(referenceDataRepositoryPort);
    }
}
