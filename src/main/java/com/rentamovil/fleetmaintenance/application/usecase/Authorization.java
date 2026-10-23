package com.rentamovil.fleetmaintenance.application.usecase;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.common.exception.ForbiddenException;

// The gateway already verified the token, but what each role may do here is this service's call
// (07-api/authentication.md -> "Who validates what")
final class Authorization {

    private Authorization() {
    }

    static void require(AuthenticatedUser caller, String permission) {
        if (!caller.hasPermission(permission)) {
            throw new ForbiddenException();
        }
    }
}
