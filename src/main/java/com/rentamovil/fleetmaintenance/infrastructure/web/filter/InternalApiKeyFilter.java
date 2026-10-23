package com.rentamovil.fleetmaintenance.infrastructure.web.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

// Protects /internal/**: only another microservice on the internal network should be able to call
// it, never an external client. The api-gateway already doesn't route /internal/**, but this is a
// second line of defense in case someone reaches the container's port directly (security audit,
// finding A2: published ports without a 127.0.0.1 bind + /internal/** with no auth of its own).
@Slf4j
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String expectedKey;

    public InternalApiKeyFilter(@Value("${app.internal.api-key}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String providedKey = request.getHeader("X-Internal-Key");
        if (expectedKey == null || expectedKey.isBlank() || !expectedKey.equals(providedKey)) {
            log.warn("Call to {} without a valid X-Internal-Key (ip={})", request.getRequestURI(), request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Missing or invalid internal API key\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }
}
