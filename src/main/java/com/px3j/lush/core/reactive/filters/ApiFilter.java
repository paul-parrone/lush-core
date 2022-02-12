package com.px3j.lush.core.reactive.filters;

import com.px3j.lush.core.api.ApiResponse;
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
public class ApiFilter implements WebFilter {
    private final Tracer tracer;

    @Autowired
    public ApiFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, WebFilterChain webFilterChain) {
        final String requestKey = generateRequestKey();
        ApiResponse response = new ApiResponse(requestKey, 200, "");

        // Set up the thread local ApiContext object - this will be used by the decorator to handle the reqeust/response
        CarryingApiContext apiContext = (CarryingApiContext) ThreadLocalApiContext.get();
        apiContext.setRequestKey( requestKey );
        apiContext.setResponse( response );
        apiContext.setExchange( exchange );

        return webFilterChain.filter(exchange);
    }

    /**
     * Generate a request key that matches the current span of the Tracer
     *
     * @return A String containing the request key.
     */
    private String generateRequestKey() {
        String contextKey = "?/?";

        if( tracer.currentSpan() != null ) {
            contextKey = tracer.currentSpan().context().traceId() + "," + tracer.currentSpan().context().spanId();
        }

        return contextKey;
    }
}
