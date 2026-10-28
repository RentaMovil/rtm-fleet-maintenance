package com.rentamovil.fleetmaintenance.infrastructure.security;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidTokenException;
import com.rentamovil.fleetmaintenance.domain.port.out.TokenVerifierPort;

// Verifies iam access tokens with its static PUBLIC key: RS256 signature and expiry.
// This service can't issue tokens, only check them (07-api/authentication.md).
// It doesn't trust the gateway's X-User-Id / X-User-Role: it re-verifies the token itself.
@Component
public class JwtVerifierAdapter implements TokenVerifierPort {

    private final RSAKey iamPublicKey;

    public JwtVerifierAdapter(RSAKey iamPublicKey) {
        this.iamPublicKey = iamPublicKey;
    }

    @Override
    public AuthenticatedUser verify(String accessToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(accessToken);

            // RS256 only: a token with "alg": "none" or HS256 is rejected
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())
                    || !jwt.verify(new RSASSAVerifier(iamPublicKey.toRSAPublicKey()))) {
                throw new InvalidTokenException("Invalid or expired token");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new InvalidTokenException("Invalid or expired token");
            }

            List<String> permissions = claims.getStringListClaim("permissions");
            return new AuthenticatedUser(
                    Integer.valueOf(claims.getSubject()),
                    claims.getStringClaim("role"),
                    permissions == null ? List.of() : permissions);
        } catch (ParseException | JOSEException | NumberFormatException e) {
            throw new InvalidTokenException("Invalid or expired token");
        }
    }
}
