package com.px3j.lush.service.endpoint.http.reactive;

import com.px3j.lush.service.ResultAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Filter that will set up each request for consumption down the line.
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
        ResultAdvice response = new ResultAdvice(requestKey, 200);

        // Set up the thread local ApiContext object - this will be used by the decorator to handle the reqeust/response
        CarryingContext context = (CarryingContext) ThreadLocalApiContext.get();
        context.setTraceId( requestKey );
        context.setAdvice( response );
        context.setExchange( exchange );

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
