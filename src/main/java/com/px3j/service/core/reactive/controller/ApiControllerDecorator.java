package com.px3j.service.core.reactive.controller;

import com.google.gson.Gson;
import com.px3j.service.core.api.ApiResponse;
import com.px3j.service.core.api.ApiException;
import com.px3j.service.core.api.ApiUnexpectedError;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;

import static com.px3j.service.core.api.ApiConstants.RESPONSE_HEADER_NAME;

@Aspect
@Component
public class ApiControllerDecorator {
    public ApiControllerDecorator() {
    }

    @Around("execution(public reactor.core.publisher.Mono com.px3j.*.controller..*(..))")
    public Mono simpleMonoApiAdvice(ProceedingJoinPoint pjp) {
        return (Mono)decoratorImpl(pjp, false);
    }

    @Around("execution(public reactor.core.publisher.Flux com.px3j.*.controller..*(..))")
    public Flux simpleFluxApiAdvice(ProceedingJoinPoint pjp) {
        return (Flux)decoratorImpl(pjp, true);
    }

    @Around("execution(public reactor.core.publisher.Mono com.px3j.*.controller..*(org.springframework.web.server.ServerWebExchange, ..))")
    public Mono exchangeMonoApiAdvice(ProceedingJoinPoint pjp) {
        final ServerWebExchange exchange = findExchangeParam(pjp.getArgs());
        final ApiResponse apiResponse = new ApiResponse();

        exchange.getAttributes().put(RESPONSE_HEADER_NAME, apiResponse);
        final Mono mono = (Mono) decoratorImpl(pjp, false);
        exchange.getResponse().getHeaders().add( RESPONSE_HEADER_NAME,  new Gson().toJson(apiResponse) );

        return mono;
    }

    @Around("execution(public reactor.core.publisher.Flux com.px3j.*.controller..*(org.springframework.web.server.ServerWebExchange, ..))")
    public Flux exchangeFluxApiAdvice(ProceedingJoinPoint pjp) {
        final ServerWebExchange exchange = findExchangeParam(pjp.getArgs());
        final ApiResponse apiResponse = new ApiResponse();

        exchange.getAttributes().put(RESPONSE_HEADER_NAME, apiResponse);
        final Flux flux = (Flux)decoratorImpl(pjp, true);
        exchange.getResponse().getHeaders().add( RESPONSE_HEADER_NAME,  new Gson().toJson(apiResponse) );

        return flux;
    }


    private ServerWebExchange findExchangeParam( final Object[] args ) {
        final Optional<Object> optExchange = Arrays.stream(args).filter(o -> o instanceof ServerWebExchange).findFirst();
        ServerWebExchange exchange = (ServerWebExchange) optExchange.orElse(null);

        if( null == exchange ) throw new ApiException("500", "Missing exchange" );
        return exchange;
    }

    private Object decoratorImpl(ProceedingJoinPoint pjp, boolean fluxOnError ) {
        try {
            final Object pjpResult = pjp.proceed();
            return pjpResult;
        }
        // Catch all error handler.  Simply returns an error Mono
        catch (final Throwable throwable) {
            final String message = null == throwable.getMessage() ? "Unexpected Error" : throwable.getMessage();
            ApiUnexpectedError err = new ApiUnexpectedError("todo", "550", message );
            return fluxOnError ? Flux.just(err) : Mono.just( err );
        }
    }
}
