package com.rentamovil.fleetmaintenance.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rentamovil.fleetmaintenance.infrastructure.web.auth.CurrentUserArgumentResolver;

import lombok.RequiredArgsConstructor;

// Registers @CurrentUser so Spring knows how to fill that controller parameter
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
