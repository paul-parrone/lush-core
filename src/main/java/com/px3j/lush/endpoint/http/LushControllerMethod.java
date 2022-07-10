package com.px3j.lush.endpoint.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lush annotation to be used to inject Lush related functionality into a controller method.  Lush will automatically
 * decorate methods that have this annotation and that return either a Mono or a Flux.
 *
 * @see com.px3j.lush.app.controller.SmokeTestController
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LushControllerMethod {
}
