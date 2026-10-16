package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.Branch;
import com.rentamovil.fleetmaintenance.domain.port.out.BranchRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ListBranchesUseCaseImplTest {

    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @InjectMocks
    private ListBranchesUseCaseImpl listBranchesUseCase;

    @Test
    void returnsEveryBranchFromTheRepository() {
        Branch centro = Branch.builder().id(1).name("Centro").build();
        Branch norte = Branch.builder().id(2).name("Norte").build();
        when(branchRepositoryPort.findAll()).thenReturn(List.of(centro, norte));

        assertThat(listBranchesUseCase.listBranches()).containsExactly(centro, norte);
    }

    @Test
    void emptyWhenThereAreNoBranches() {
        when(branchRepositoryPort.findAll()).thenReturn(List.of());

        assertThat(listBranchesUseCase.listBranches()).isEmpty();
    }
}
