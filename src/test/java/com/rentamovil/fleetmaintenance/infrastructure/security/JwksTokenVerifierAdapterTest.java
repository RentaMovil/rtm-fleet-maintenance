package com.rentamovil.fleetmaintenance.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.fleetmaintenance.domain.exception.InvalidTokenException;

class JwksTokenVerifierAdapterTest {

    private static RSAKey iamKey;
    private static RSAKey otherKey;
    private static JwksTokenVerifierAdapter verifier;

    @BeforeAll
    static void setUp() throws Exception {
        iamKey = new RSAKeyGenerator(2048).keyID("iam").generate();
        otherKey = new RSAKeyGenerator(2048).keyID("iam").generate();
        //What iam would publish at /jwks: the public part only
        verifier = new JwksTokenVerifierAdapter(new ImmutableJWKSet<>(new JWKSet(iamKey.toPublicJWK())));
    }

    private static String token(RSAKey key, Instant expiresAt) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("12")
                .claim("role", "ADMIN")
                .claim("permissions", List.of("vehicles:create"))
                .expirationTime(Date.from(expiresAt))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    @Test
    void acceptsTokenSignedByIam() throws Exception {
        AuthenticatedUser user = verifier.verify(token(iamKey, Instant.now().plusSeconds(600)));

        assertThat(user.getUserId()).isEqualTo(12);
        assertThat(user.getRole()).isEqualTo("ADMIN");
        assertThat(user.hasPermission("vehicles:create")).isTrue();
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String expired = token(iamKey, Instant.now().minusSeconds(600));

        assertThatThrownBy(() -> verifier.verify(expired)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() throws Exception {
        String forged = token(otherKey, Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> verifier.verify(forged)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rejectsHs256() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("12")
                .expirationTime(Date.from(Instant.now().plusSeconds(600))).build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner("a-shared-secret-of-at-least-32-bytes!!!!"));

        assertThatThrownBy(() -> verifier.verify(jwt.serialize())).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rejectsGarbage() {
        assertThatThrownBy(() -> verifier.verify("not-a-jwt")).isInstanceOf(InvalidTokenException.class);
    }
}
