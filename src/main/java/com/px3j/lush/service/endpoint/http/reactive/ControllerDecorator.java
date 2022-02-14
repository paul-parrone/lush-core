package com.px3j.lush.service.endpoint.http.reactive;

import com.google.gson.Gson;
import com.px3j.lush.core.exception.StackTraceToLoggerWriter;
import com.px3j.lush.service.Constants;
import com.px3j.lush.service.Context;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
    public Mono simpleMonoApiAdvice(ProceedingJoinPoint pjp) {
        return (Mono)decoratorImpl(pjp, false);
    }

    @Around("execution(public reactor.core.publisher.Flux com.px3j..controller..*(..))")
    public Flux simpleFluxApiAdvice(ProceedingJoinPoint pjp) {
        return (Flux)decoratorImpl(pjp, true);
    }

    private Object decoratorImpl(ProceedingJoinPoint pjp, boolean fluxOnError ) {
        CarryingContext apiContext = (CarryingContext)ThreadLocalApiContext.get();

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

            apiContext.getResponse().setStatusCode( -999 );

            addResponseHeader( apiContext );
            return fluxOnError ? Flux.empty() : Mono.empty();
        }
        finally {
            log.debug( "finally done" );
        }
    }

    private void injectApiContext( Method method, ProceedingJoinPoint pjp, Context context) {
        int index = 0;

        for( Parameter p : method.getParameters() ) {
            if( p.getType() == Context.class ) {
                // Set the values on the context in the args array...
                Context contextArg = (Context) pjp.getArgs()[index];
                contextArg.setTraceId( context.getTraceId() );
                contextArg.setResponse( context.getResponse() );
            }

            index++;
        }
    }

    private void addResponseHeader( CarryingContext context ) {
        context.getExchange()
                .getResponse()
                .getHeaders()
                .add( Constants.RESPONSE_HEADER_NAME, new Gson().toJson(context.getResponse()) );
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
