package com.rentamovil.fleetmaintenance.infrastructure.web.auth;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.rentamovil.fleetmaintenance.domain.entity.AuthenticatedUser;
import com.rentamovil.common.exception.InvalidTokenException;
import com.rentamovil.fleetmaintenance.domain.port.out.TokenVerifierPort;

import lombok.RequiredArgsConstructor;

// Reads the access token from the Authorization header, validates it and hands the AuthenticatedUser
// to parameters marked with @CurrentUser. If it's missing or invalid the answer is 401.
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BEARER = "Bearer ";

    private final TokenVerifierPort tokenVerifierPort;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String authorization = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER)) {
            if (!parameter.getParameterAnnotation(CurrentUser.class).required()) {
                return null;
            }
            throw new InvalidTokenException("Missing access token");
        }
        return tokenVerifierPort.verify(authorization.substring(BEARER.length()));
    }
}
