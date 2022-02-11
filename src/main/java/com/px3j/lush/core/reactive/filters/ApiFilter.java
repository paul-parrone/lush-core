package com.px3j.lush.core.reactive.filters;

import com.px3j.lush.core.api.ApiConstants;
import com.px3j.lush.core.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;

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

        // Set up the thread local ApiContext object - will be used by the decorator to handle the reqeust/response
        CarryingApiContext apiContext = (CarryingApiContext) ThreadLocalApiContext.get();
        apiContext.setRequestKey( requestKey );
        apiContext.setResponse( response );
        apiContext.setExchange( exchange );

        return webFilterChain.filter(exchange);
//        return webFilterChain.filter(decorate(exchange));
    }

    private String generateRequestKey() {
        String contextKey = "?/?";

        if( tracer.currentSpan() != null ) {
            contextKey = tracer.currentSpan().context().traceId() + "," + tracer.currentSpan().context().spanId();
        }

        return contextKey;
    }

//    private ServerWebExchange decorate(ServerWebExchange exchange) {
//        return new ApiServerWebExchangeDecorator(exchange);
//    }
}


final class ApiServerWebExchangeDecorator extends ServerWebExchangeDecorator {
    private final ServerHttpResponseDecorator responseDecorator;

    public ApiServerWebExchangeDecorator(ServerWebExchange delegate) {
        super(delegate);
        this.responseDecorator = new ApiServerHttpResponseDecorator(delegate.getResponse());
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}

class ApiServerHttpResponseDecorator extends ServerHttpResponseDecorator {
    private final StringBuilder cachedBody = new StringBuilder();

    public ApiServerHttpResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(Mono.from(body).doOnNext(
                this::addApiHeader
        ));
    }

    private void addApiHeader(DataBuffer buffer) {
        cachedBody.append(UTF_8.decode(buffer.asByteBuffer()).toString());
        if( cachedBody.indexOf("unexpectedError") != -1 ) {
            getDelegate().getHeaders().add( ApiConstants.RESPONSE_HEADER_NAME, cachedBody.toString() );
        }
    }
}
