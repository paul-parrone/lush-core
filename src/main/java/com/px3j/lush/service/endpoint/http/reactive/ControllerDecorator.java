package com.px3j.lush.service.endpoint.http.reactive;

import com.google.gson.Gson;
import com.px3j.lush.core.exception.StackTraceToLoggerWriter;
import com.px3j.lush.core.security.Actor;
import com.px3j.lush.service.LushContext;
import com.px3j.lush.service.ResultAdvice;
import com.px3j.lush.service.endpoint.http.Constants;
import com.px3j.lush.service.endpoint.http.security.reactive.Passport;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
@Aspect
@Component()
public class ControllerDecorator {
    private final Tracer tracer;

    @Autowired
    public ControllerDecorator(Tracer tracer ) {
        this.tracer = tracer;
    }

    @Around("execution(public reactor.core.publisher.Mono com.px3j..controller..*(..))")
    public Mono monoInvocationAdvice(ProceedingJoinPoint pjp) {
        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (Passport)sc.getAuthentication() )
                .map(passport -> {
                    Actor actor = passport.getActor();
                    MDC.put( "username", actor.getUsername() );
                    return actor;
                })
                .flatMap( (actor) -> (Mono)decoratorImpl(pjp, actor,false) );
    }

    @Around("execution(public reactor.core.publisher.Flux com.px3j..controller..*(..))")
    public Flux fluxInvocationAdvice(ProceedingJoinPoint pjp) {
        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (Passport)sc.getAuthentication() )
                .map(passport -> {
                    Actor actor = passport.getActor();
                    MDC.put( "username", actor.getUsername() );
                    return actor;
                })
                .flatMapMany( (actor) -> (Flux)decoratorImpl(pjp, actor,true) );
    }

    private Object decoratorImpl(ProceedingJoinPoint pjp, Actor actor, boolean fluxOnError ) {
        CarryingContext apiContext = (CarryingContext)ThreadLocalApiContext.get();

        try {
            Method method = getMethodBeingCalled(pjp);

            injectLushContext(method, pjp, apiContext);
            injectActor( method, pjp, actor );

            Object returnValue = pjp.proceed();
            addResponseHeader( apiContext );
            return returnValue;
        }

        // Catch all error handler.  Simply returns an error Mono or Flux
        catch (final Throwable throwable) {
            throwable.printStackTrace( new StackTraceToLoggerWriter(log) );

            ResultAdvice advice = apiContext.getAdvice();
            advice.setStatusCode( -999 );
            advice.putExtra( "isUnexpectedException", true );

            addResponseHeader( apiContext );
            return fluxOnError ? Flux.empty() : Mono.empty();
        }
    }

    private void injectActor( Method method, ProceedingJoinPoint pjp, Actor actor ) {
        findArgumentIndex( method, Actor.class )
                .ifPresent( (i) -> {
                    Actor contextArg = (Actor) pjp.getArgs()[i];
                    contextArg.populateFrom( actor );
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
        context.getExchange()
                .getResponse()
                .getHeaders()
                .add( Constants.RESPONSE_HEADER_NAME, new Gson().toJson(context.getAdvice()) );
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
