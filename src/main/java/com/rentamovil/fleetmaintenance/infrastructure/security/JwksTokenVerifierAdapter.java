package com.rentamovil.fleetmaintenance.infrastructure.security;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.exception.DependencyUnavailableException;
import com.rentamovil.common.exception.InvalidTokenException;
import com.rentamovil.fleetmaintenance.domain.port.out.TokenVerifierPort;

// Verifies iam access tokens with its PUBLIC key (GET iam/jwks): RS256 signature and expiry.
// This service can't issue tokens, only check them (07-api/authentication.md).
// It doesn't trust the gateway's X-User-Id / X-User-Role: it re-verifies the token itself.
@Component
public class JwksTokenVerifierAdapter implements TokenVerifierPort {

    private final DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

    // Keys are fetched from iam when the first token arrives and then cached; if a token with an
    // unknown kid arrives (iam rotated its key) they are fetched again automatically.
    // That's why the service starts even if iam isn't up yet.
    @Autowired
    public JwksTokenVerifierAdapter(@Value("${app.fleet.iam-url}") String iamUrl) throws MalformedURLException {
        this(JWKSourceBuilder.create(URI.create(iamUrl + "/jwks").toURL()).build());
    }

    // For tests: in-memory keys instead of downloading them from iam
    JwksTokenVerifierAdapter(JWKSource<SecurityContext> keys) {
        //RS256 only: a token with "alg": "none" or HS256 is rejected
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keys));
    }

    @Override
    public AuthenticatedUser verify(String accessToken) {
        try {
            //process checks signature and expiry (exp)
            JWTClaimsSet claims = processor.process(accessToken, null);
            List<String> permissions = claims.getStringListClaim("permissions");
            return new AuthenticatedUser(
                    Integer.valueOf(claims.getSubject()),
                    claims.getStringClaim("role"),
                    permissions == null ? List.of() : permissions);
        } catch (KeySourceException e) {
            //iam is down: not the token's fault
            throw new DependencyUnavailableException("IAM_UNAVAILABLE",
                    "Could not fetch iam keys to verify the token", e);
        } catch (ParseException | BadJOSEException | JOSEException | NumberFormatException e) {
            throw new InvalidTokenException("Invalid or expired token");
        }
    }
}
