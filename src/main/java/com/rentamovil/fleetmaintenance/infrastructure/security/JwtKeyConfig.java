package com.rentamovil.fleetmaintenance.infrastructure.security;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * Loads ONLY iam's RS256 public key at startup.
 *
 * <p>This service never signs tokens, so it doesn't need the private key.
 */
@Configuration
public class JwtKeyConfig {

    @Bean
    public RSAKey iamPublicKey(@Value("${app.fleet.jwt.public-key}") String publicKeyText) throws Exception {

        if (publicKeyText.isBlank()) {
            throw new IllegalStateException("Missing APP_FLEET_MAINTENANCE_JWT_PUBLIC_KEY in the .env");
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(decode(publicKeyText)));

        return new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("rentamovil-iam")
                .build();
    }

    /** Accepts either the full PEM or just the base64 text on one line (fits the .env). */
    private byte[] decode(String keyText) {
        String base64 = keyText.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
