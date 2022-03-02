package com.px3j.lush.service.endpoint.http.reactive;

import com.google.gson.Gson;
import com.px3j.lush.core.exception.StackTraceToLoggerWriter;
import com.px3j.lush.core.LushContext;
import com.px3j.lush.core.ResultAdvice;
import com.px3j.lush.service.endpoint.http.Constants;
import com.px3j.lush.core.security.Passport;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Aspect that will intercept calls to Lush based controllers.
 *
 * @author Paul Parrone
 */
@Slf4j
@Aspect
@Component()
public class ControllerDecorator {
    private final Tracer tracer;

    @Autowired
    public ControllerDecorator(Tracer tracer ) {
        this.tracer = tracer;
    }

    @Around("execution(public reactor.core.publisher.Mono com..lush..controller..*(..))")
    public Mono monoInvocationAdvice(ProceedingJoinPoint pjp) {
        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (Passport) sc.getAuthentication().getPrincipal() )
                .map(passport -> {
                    MDC.put( "username", passport.getUsername() );
                    return passport;
                })
                .flatMap( (passport) -> (Mono)decoratorImpl(pjp, passport,false) );
    }

    @Around("execution(public reactor.core.publisher.Flux com..lush..controller..*(..))")
    public Flux fluxInvocationAdvice(ProceedingJoinPoint pjp) {
        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (Passport) sc.getAuthentication().getPrincipal() )
                .map(passport -> {
                    MDC.put( "username", passport.getUsername() );
                    return passport;
                })
                .flatMapMany( (passport) -> (Flux)decoratorImpl(pjp, passport,true) );
    }

    private Object decoratorImpl(ProceedingJoinPoint pjp, Passport passport, boolean fluxOnError ) {
        if( log.isDebugEnabled() ) {
            log.debug( "START: Lush interception" );
        }

        CarryingContext apiContext = (CarryingContext)ThreadLocalApiContext.get();

        try {
            // Get the target method from the join point, use this to inject parameters.
            Method method = getMethodBeingCalled(pjp);

            // If the method declares an argument of LushContext, inject it (we inject it by copying the values)
            injectLushContext( method, pjp, apiContext );
            // If the method declares an argument of Passport, inject it (we inject it by copying the values)
            injectPassport( method, pjp, passport );

            // Invoke the target method
            Object returnValue = pjp.proceed();

            // Add the Lush response header
            addResponseHeader( apiContext );
            return returnValue;
        }

        // Catch all error handler.  Returns an empty Mono or Flux
        catch (final Throwable throwable) {
            throwable.printStackTrace( new StackTraceToLoggerWriter(log) );

            ResultAdvice advice = apiContext.getAdvice();
            advice.setStatusCode( -999 );
            advice.putExtra( "isUnexpectedException", true );

            addResponseHeader( apiContext );
            return fluxOnError ? Flux.empty() : Mono.empty();
        }
        finally {
            if( log.isDebugEnabled() ) {
                log.debug( "END: Lush interception" );
            }
        }
    }

    private void injectPassport(Method method, ProceedingJoinPoint pjp, Passport passport ) {
        findArgumentIndex( method, Passport.class )
                .ifPresent( (i) -> {
                    Passport contextArg = (Passport) pjp.getArgs()[i];
                    contextArg.populateFrom( passport );
                });
    }

    /**
     * Helper method to inject the LushContext if the method being called requests it.
     *
     * @param method The method being called
     * @param pjp The joinpoint.
     * @param lushContext The context instance to inject.
     */
    private void injectLushContext(Method method, ProceedingJoinPoint pjp, LushContext lushContext) {
        findArgumentIndex( method, LushContext.class )
                .ifPresent( (i) -> {
                    LushContext contextArg = (LushContext) pjp.getArgs()[i];
                    contextArg.setTraceId( lushContext.getTraceId() );
                    contextArg.setAdvice( lushContext.getAdvice() );
                });

/*
        int index = 0;

        for( Parameter p : method.getParameters() ) {
            if( p.getType() == LushContext.class ) {
                // Set the values on the context in the args array...
                LushContext contextArg = (LushContext) pjp.getArgs()[index];
                contextArg.setTraceId( lushContext.getTraceId() );
                contextArg.setAdvice( lushContext.getAdvice() );
            }

            index++;
        }
*/
    }

    private Optional<Integer> findArgumentIndex(Method method, Class clazz ) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for( int i=0; i<parameterTypes.length; i++ ) {
            if( parameterTypes[i] == clazz ) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    /**
     * Helper method to add the response instance to the response header.
     *
     * @param context The context holding the ResponseAdvice to add to the HTTP response header.
     */
    private void addResponseHeader( CarryingContext context ) {
        HttpHeaders headers = context.getExchange()
                .getResponse()
                .getHeaders();

        headers.add( "Access-Control-Expose-Headers", Constants.ADVICE_HEADER_NAME);
        headers.add( Constants.ADVICE_HEADER_NAME, new Gson().toJson(context.getAdvice()) );
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
