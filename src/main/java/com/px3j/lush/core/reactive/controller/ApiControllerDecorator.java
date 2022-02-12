package com.px3j.lush.core.reactive.controller;

import com.google.gson.Gson;
import com.px3j.lush.core.api.*;
import com.px3j.lush.core.exception.StackTraceToLoggerWriter;
import com.px3j.lush.core.reactive.filters.CarryingApiContext;
import com.px3j.lush.core.reactive.filters.ThreadLocalApiContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Aspect
@Component()
public class ApiControllerDecorator {
    private final Tracer tracer;

    @Autowired
    public ApiControllerDecorator( Tracer tracer ) {
        this.tracer = tracer;
    }

    @Around("execution(public reactor.core.publisher.Mono com.px3j..controller..*(..))")
    public Mono simpleMonoApiAdvice(ProceedingJoinPoint pjp) {
        return (Mono)decoratorImpl(pjp, false);
    }

    @Around("execution(public reactor.core.publisher.Flux com.px3j..controller..*(..))")
    public Flux simpleFluxApiAdvice(ProceedingJoinPoint pjp) {
        return (Flux)decoratorImpl(pjp, true);
    }

    private Object decoratorImpl(ProceedingJoinPoint pjp, boolean fluxOnError ) {
        CarryingApiContext apiContext = (CarryingApiContext)ThreadLocalApiContext.get();

        try {
            Method method = getMethodBeingCalled(pjp);
            injectApiContext(method, pjp, apiContext);

            Object returnValue = pjp.proceed();
            addResponseHeader( apiContext );
            return returnValue;
        }

        // Catch all error handler.  Simply returns an error Mono or Flux
        catch (final Throwable throwable) {
            throwable.printStackTrace( new StackTraceToLoggerWriter(log) );

            final String message = null == throwable.getMessage() ? "Unexpected Error" : throwable.getMessage();

            apiContext.getResponse().setDisplayableMessage( message );
            apiContext.getResponse().setStatusCode( -999 );

            addResponseHeader( apiContext );
            return fluxOnError ? Flux.empty() : Mono.empty();
        }
        finally {
            log.debug( "finally done" );
        }
    }

    private void injectApiContext( Method method, ProceedingJoinPoint pjp, ApiContext apiContext ) {
        int index = 0;

        for( Parameter p : method.getParameters() ) {
            if( p.getType() == ApiContext.class ) {
                // Set the values on the context in the args array...
                ApiContext contextArg = (ApiContext) pjp.getArgs()[index];
                contextArg.setRequestKey( apiContext.getRequestKey() );
                contextArg.setResponse( apiContext.getResponse() );
            }

            index++;
        }
    }

    private void addResponseHeader( CarryingApiContext context ) {
        context.getExchange()
                .getResponse()
                .getHeaders()
                .add( ApiConstants.RESPONSE_HEADER_NAME, new Gson().toJson(context.getResponse()) );
    }

    private Method getMethodBeingCalled(ProceedingJoinPoint pjp ) throws NoSuchMethodException, SecurityException {
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        Method method = signature.getMethod();

        if( !method.getDeclaringClass().isInterface() ) {
            return method;
        }

        return pjp.getTarget().getClass().getDeclaredMethod( signature.getName(), method.getParameterTypes() );
    }

}
