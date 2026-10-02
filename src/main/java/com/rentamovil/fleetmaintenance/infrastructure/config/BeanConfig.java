package com.rentamovil.fleetmaintenance.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rentamovil.fleetmaintenance.application.usecase.ListReferenceDataUseCaseImpl;
import com.rentamovil.fleetmaintenance.domain.port.in.ListReferenceDataUseCase;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;

// Wires the use cases to their adapters.
// Use cases have no @Service so the domain doesn't depend on Spring.
@Configuration
public class BeanConfig {

    @Bean
    public ListReferenceDataUseCase listReferenceDataUseCase(ReferenceDataRepositoryPort referenceDataRepositoryPort) {
        return new ListReferenceDataUseCaseImpl(referenceDataRepositoryPort);
    }
}
