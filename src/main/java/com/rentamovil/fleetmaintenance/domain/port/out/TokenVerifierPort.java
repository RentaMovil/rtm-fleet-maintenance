package com.rentamovil.fleetmaintenance.domain.port.out;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;

// Verifies an access token issued by iam (RS256 signature and expiry) and tells who it is.
// This service never issues tokens. Throws InvalidTokenException if it's not valid.
public interface TokenVerifierPort {

    AuthenticatedUser verify(String accessToken);
}
