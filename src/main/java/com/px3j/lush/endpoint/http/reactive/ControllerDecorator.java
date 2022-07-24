package com.px3j.lush.endpoint.http.reactive;

import brave.baggage.BaggageField;
import com.px3j.lush.core.model.LushAdvice;
import com.px3j.lush.core.exception.StackTraceToLoggerWriter;
import com.px3j.lush.core.model.LushContext;
import com.px3j.lush.core.ticket.LushTicket;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.reactivestreams.Publisher;
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
@Component
@Slf4j( topic = "lush.core.debug")
public class ControllerDecorator {
    private final BaggageField lushUserNameField;

    @Autowired
    public ControllerDecorator(BaggageField lushUserNameField) {
        this.lushUserNameField = lushUserNameField;
    }

    @Pointcut("@annotation(com.px3j.lush.endpoint.http.LushControllerMethod)")
    public void lushControllerMethods() {}

    @Around("lushControllerMethods() && execution(public reactor.core.publisher.Mono *..*(..))")
    public Mono monoInvocationAdvice(ProceedingJoinPoint pjp) {
        if( log.isDebugEnabled() ) {
            log.debug( "****" );
            log.debug( "intercepted request - Mono invocation" );
        }

        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (LushTicket) sc.getAuthentication().getPrincipal() )
                .map(ticket -> {
                    if( log.isDebugEnabled() ) log.debug( "ticket user: " + ticket.getUsername() );
                    lushUserNameField.updateValue(ticket.getUsername());
                    return ticket;
                })
                .flatMap( (ticket) -> (Mono)decoratorImpl(pjp, ticket,false) );
    }

    @Around("lushControllerMethods() && execution(public reactor.core.publisher.Flux *..*(..))")
    public Flux fluxInvocationAdvice(ProceedingJoinPoint pjp) {
        if( log.isDebugEnabled() ) {
            log.debug("****");
            log.debug("intercepted request - Flux invocation");
        }

        return ReactiveSecurityContextHolder.getContext()
                .map( sc -> (LushTicket) sc.getAuthentication().getPrincipal() )
                .map(ticket -> {
                    if( log.isDebugEnabled() ) log.debug( "ticket user: " + ticket.getUsername() );
                    lushUserNameField.updateValue(ticket.getUsername());
                    return ticket;
                })
                .flatMapMany( (ticket) -> (Flux)decoratorImpl(pjp, ticket,true) );
    }

    private Object decoratorImpl(ProceedingJoinPoint pjp, LushTicket ticket, boolean fluxOnError ) {
        CarryingContext apiContext = (CarryingContext)ThreadLocalApiContext.get();

        try {
            // Get the target method from the join point, use this to inject parameters.
            Method method = getMethodBeingCalled(pjp);

            // If the method declares an argument of LushContext, inject it (we inject it by copying the values)
            injectLushContext( method, pjp, apiContext );
            // If the method declares an argument of LushTicket, inject it (we inject it by copying the values)
            injectTicket( method, pjp, ticket);

            // Invoke the target method wrapped in a publisher - this allows us to handle exceptions in the Lush way
            if( fluxOnError ) {
                return Flux.from((Publisher<?>) pjp.proceed())
                        .onErrorResume( throwable -> {
                            errorHandler(apiContext, throwable);
                            return Flux.empty();
                        })
                        .doOnComplete( () -> {
                            if( log.isDebugEnabled() ) log.debug( "****" );
                        });
            }
            else {
                return Mono.from((Publisher<?>) pjp.proceed())
                        .onErrorResume( throwable -> {
                            errorHandler(apiContext, throwable);
                            return Mono.empty();
                        })
                        .doOnSuccess( o -> {
                            if( log.isDebugEnabled() ) log.debug( "****" );
                        });
            }
        }

        // Catch all error handler.  Returns an empty Mono or Flux
        catch (final Throwable throwable) {
            throwable.printStackTrace( new StackTraceToLoggerWriter(log) );

            LushAdvice advice = apiContext.getAdvice();
            advice.setStatusCode( -999 );
            advice.putExtra( "lush.isUnexpectedException", true );

            if( log.isDebugEnabled() ) log.debug( "****" );
            return fluxOnError ? Flux.empty() : Mono.empty();
        }
    }

    /**
     * Helper method to populate the returned LushAdvice properly in the event that an unexpected exception occurs during
     * this call.
     *
     * @param lushContext The context to populate.
     * @param throwable The exception causing the error.
     */
    private void errorHandler(LushContext lushContext, Throwable throwable ) {
        throwable.printStackTrace( new StackTraceToLoggerWriter(log) );

        LushAdvice advice = lushContext.getAdvice();
        if( advice != null ) {
            log.warn( "advice is null in context - cannot set status codes" );
            advice.setStatusCode( -999 );
            advice.putExtra( "lush.isUnexpectedException", true );
        }
    }

    /**
     * Helper method to inject the LushTicket if the method being called requests it.
     *
     * @param method The method being called.
     * @param pjp The joinpoint.
     * @param ticket The ticket instance to inject.
     */
    private void injectTicket(Method method, ProceedingJoinPoint pjp, LushTicket ticket) {
        findArgumentIndex( method, LushTicket.class )
                .ifPresent( (i) -> {
                    LushTicket contextArg = (LushTicket) pjp.getArgs()[i];
                    contextArg.populateFrom(ticket);
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
     * @throws NoSuchMethodException If the method being called cannot be found.
     * @throws SecurityException If the method cannot be accessed.
     */
    private Method getMethodBeingCalled(ProceedingJoinPoint pjp ) throws NoSuchMethodException, SecurityException {
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        Method method = signature.getMethod();

        if( method.getDeclaringClass().isInterface() ) {
            method = pjp.getTarget().getClass().getDeclaredMethod( signature.getName(), method.getParameterTypes() );
        }

        if( log.isDebugEnabled() ) {
            log.debug( String.format( "invoking: %s::%s", method.getDeclaringClass(), method.getName()));
        }

        return method;
    }
}
