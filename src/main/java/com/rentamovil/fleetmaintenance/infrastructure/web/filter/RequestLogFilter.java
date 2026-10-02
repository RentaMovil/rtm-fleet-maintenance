package com.rentamovil.fleetmaintenance.infrastructure.web.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

// One log line per request that reaches fleet-maintenance, e.g.:
//   POST /vehicles -> 400 (15 ms) corr=3f2a...
// corr (X-Correlation-Id) is set by the gateway: it finds the same request in both logs.
// No bodies or headers are logged: passwords and tokens never show up in the log.
@Slf4j
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            String correlationId = request.getHeader("X-Correlation-Id");
            log.info("{} {} -> {} ({} ms) corr={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    System.currentTimeMillis() - start,
                    correlationId != null ? correlationId : "-");
        }
    }

    // Docker's HEALTHCHECK and the gateway's /health call /health often: not logged
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/health".equals(request.getRequestURI());
    }
}
