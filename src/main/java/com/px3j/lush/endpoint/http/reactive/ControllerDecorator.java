package com.px3j.lush.endpoint.http.reactive;

import brave.baggage.BaggageField;
import com.px3j.lush.core.model.Advice;
import com.px3j.lush.core.exception.StackTraceToLoggerWriter;
import com.px3j.lush.core.model.LushContext;
import com.px3j.lush.core.passport.Passport;
import com.px3j.lush.core.util.WithLushDebug;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Decorator, applied via AOP, that intercepts calls to any Lush based controllers.  It silently intercepts and provides
 * the Lush functionality.
 *
 * @author Paul Parrone
 */
@Aspect
@Component()
public class ControllerDecorator implements WithLushDebug {
    private final BaggageField lushUserNameField;

    private final Logger lushDebug;

    @Autowired
    public ControllerDecorator(BaggageField lushUserNameField, Logger lushDebug) {
        this.lushUserNameField = lushUserNameField;
        this.lushDebug = lushDebug;
    }

    @Around("execution(public reactor.core.publisher.Mono com..lush..controller..*(..))")
    public Mono monoInvocationAdvice(ProceedingJoinPoint pjp) {
        if( isDbg() ) {
            log( "****" );
            log( "intercepted request - Mono invocation" );
        }

        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (Passport) sc.getAuthentication().getPrincipal() )
                .map(passport -> {
                    if( isDbg() ) log( "passport user: " + passport.getUsername() );
                    lushUserNameField.updateValue(passport.getUsername());
                    return passport;
                })
                .flatMap( (passport) -> (Mono)decoratorImpl(pjp, passport,false) );
    }

    @Around("execution(public reactor.core.publisher.Flux com..lush..controller..*(..))")
    public Flux fluxInvocationAdvice(ProceedingJoinPoint pjp) {
        if( isDbg() ) {
            log("****");
            log("intercepted request - Flux invocation");
        }

        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (Passport) sc.getAuthentication().getPrincipal() )
                .map(passport -> {
                    if( isDbg() ) log( "passport user: " + passport.getUsername() );
                    lushUserNameField.updateValue(passport.getUsername());
                    return passport;
                })
                .flatMapMany( (passport) -> (Flux)decoratorImpl(pjp, passport,true) );
    }

    @Override
    public Logger getLushDebug() {
        return lushDebug;
    }

    private Object decoratorImpl(ProceedingJoinPoint pjp, Passport passport, boolean fluxOnError ) {
        CarryingContext apiContext = (CarryingContext)ThreadLocalApiContext.get();

        try {
            // Get the target method from the join point, use this to inject parameters.
            Method method = getMethodBeingCalled(pjp);

            // If the method declares an argument of LushContext, inject it (we inject it by copying the values)
            injectLushContext( method, pjp, apiContext );
            // If the method declares an argument of Passport, inject it (we inject it by copying the values)
            injectPassport( method, pjp, passport );

            // Invoke the target method wrapped in a publisher - this allows us to handle exceptions in the Lush way
            if( fluxOnError ) {
                return Flux.from((Publisher<?>) pjp.proceed())
                        .onErrorResume( throwable -> {
                            errorHandler(apiContext, throwable);
                            return Flux.empty();
                        })
                        .doOnComplete( () -> {
                            if( isDbg() ) log( "****" );
                        });
            }
            else {
                return Mono.from((Publisher<?>) pjp.proceed())
                        .onErrorResume( throwable -> {
                            errorHandler(apiContext, throwable);
                            return Mono.empty();
                        })
                        .doOnSuccess( o -> {
                            if( isDbg() ) log( "****" );
                        });
            }
        }

        // Catch all error handler.  Returns an empty Mono or Flux
        catch (final Throwable throwable) {
            throwable.printStackTrace( new StackTraceToLoggerWriter(lushDebug) );

            Advice advice = apiContext.getAdvice();
            advice.setStatusCode( -999 );
            advice.putExtra( "lush.isUnexpectedException", true );

            if( isDbg() ) log( "****" );
            return fluxOnError ? Flux.empty() : Mono.empty();
        }
    }

    /**
     * Helper method to populate the returned Advice properly in the event that an unexpected exception occurs during
     * this call.
     *
     * @param lushContext The context to populate.
     * @param throwable The exception causing the error.
     */
    private void errorHandler(LushContext lushContext, Throwable throwable ) {
        throwable.printStackTrace( new StackTraceToLoggerWriter(lushDebug) );

        Advice advice = lushContext.getAdvice();
        if( advice != null ) {
            warn( "advice is null in context - cannot set status codes" );
            advice.setStatusCode( -999 );
            advice.putExtra( "lush.isUnexpectedException", true );
        }
    }

    /**
     * Helper method to inject the Passport if the method being called requests it.
     *
     * @param method The method being called.
     * @param pjp The joinpoint.
     * @param passport The passport instance to inject.
     */
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
    }

    /**
     * Find the argument index of the parameter of type clazz (if there is one) and return the index.
     *
     * @param method The method object to check.
     * @param clazz The type of argument to find.
     *
     * @return The index if found, or empty.
     */
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
     * Extract a Method object representing the method being called from the joinpoint.
     *
     * @param pjp The joinpoint to inspect.
     * @return A Method instance represent the method being called.
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private Method getMethodBeingCalled(ProceedingJoinPoint pjp ) throws NoSuchMethodException, SecurityException {
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        Method method = signature.getMethod();

        if( method.getDeclaringClass().isInterface() ) {
            method = pjp.getTarget().getClass().getDeclaredMethod( signature.getName(), method.getParameterTypes() );
        }

        if( isDbg() ) {
            log( String.format( "invoking: %s::%s", method.getDeclaringClass(), method.getName()));
        }

        return method;
    }
}
