package com.px3j.lush.service.endpoint.http.reactive;

import com.google.gson.Gson;
import com.px3j.lush.core.Advice;
import com.px3j.lush.service.endpoint.http.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


/**
 * A filter that will apply Lush principals to a request/response.
 *
 * @author Paul Parrone
 */
@Slf4j
@Component
public class EndpointFilter implements WebFilter {
    private final Tracer tracer;

    @Autowired
    public EndpointFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, WebFilterChain webFilterChain) {
        final String requestKey = generateTraceId();
        final Advice advice = new Advice(requestKey, 200);

        // Set up the thread local ApiContext object - this will be used by the decorator to handle the reqeust
        // and response advice
        CarryingContext context = (CarryingContext) ThreadLocalApiContext.get();
        context.setTraceId( requestKey );
        context.setAdvice( advice );
        context.setExchange( exchange );

        // Set up the exchange to add the advice response header once the controller method has returned.
        exchange.getResponse().beforeCommit( () -> {
                    return Mono.deferContextual(Mono::just).doOnNext(ctx -> {
                        HttpHeaders headers = exchange.getResponse().getHeaders();
                        headers.add( "Access-Control-Expose-Headers", Constants.ADVICE_HEADER_NAME );
                        headers.add( Constants.ADVICE_HEADER_NAME, new Gson().toJson(context.getAdvice()));
                    }).then();
        });

        return webFilterChain.filter(exchange);
    }

    /**
     * Generate a request key that matches the current span of the Tracer
     *
     * @return A String containing the request key.
     */
    private String generateTraceId() {
        String contextKey = "?/?";

        if( tracer.currentSpan() != null ) {
            contextKey = tracer.currentSpan().context().traceId() + "," + tracer.currentSpan().context().spanId();
        }

        return contextKey;
    }
}
