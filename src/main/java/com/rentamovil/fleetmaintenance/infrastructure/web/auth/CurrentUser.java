package com.rentamovil.fleetmaintenance.infrastructure.web.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Put it on an AuthenticatedUser controller parameter: the endpoint requires
// "Authorization: Bearer <token>" and receives who is calling.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
