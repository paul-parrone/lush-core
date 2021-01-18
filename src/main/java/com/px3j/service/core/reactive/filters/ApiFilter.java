package com.px3j.service.core.reactive.filters;

import com.px3j.service.core.api.ApiConstants;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class ApiFilter implements WebFilter {
    static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

    @Override
    public Mono<Void> filter(final ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
//        serverWebExchange.getResponse()
//                .getHeaders().add("web-filter", "web-filter-test");

        return webFilterChain.filter(decorate(serverWebExchange));
    }

    private ServerWebExchange decorate(ServerWebExchange exchange) {
        return new ApiServerWebExchangeDecorator(exchange);

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