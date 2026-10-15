package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.port.in.ListBranchesUseCase;
import com.rentamovil.fleetmaintenance.domain.port.in.ManageBranchUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.auth.CurrentUser;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.BranchResponse;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.BranchWriteRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-FLEET-007. Listing is public (Home search); create, edit and delete require branches:manage.
@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
public class BranchController {

    private final ListBranchesUseCase listBranchesUseCase;
    private final ManageBranchUseCase manageBranchUseCase;

    @GetMapping
    public List<BranchResponse> listBranches() {
        return listBranchesUseCase.listBranches().stream().map(BranchResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(@CurrentUser AuthenticatedUser currentUser,
                                 @Valid @RequestBody BranchWriteRequest request) {
        return BranchResponse.from(manageBranchUseCase.create(currentUser, request.toData()));
    }

    @PutMapping("/{id}")
    public BranchResponse update(@CurrentUser AuthenticatedUser currentUser,
                                 @PathVariable Integer id,
                                 @Valid @RequestBody BranchWriteRequest request) {
        return BranchResponse.from(manageBranchUseCase.update(currentUser, id, request.toData()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AuthenticatedUser currentUser, @PathVariable Integer id) {
        manageBranchUseCase.delete(currentUser, id);
    }
}
