package com.px3j.lush.service.endpoint.http.reactive;

import com.px3j.lush.core.LushContext;
import lombok.Getter;
import org.springframework.web.server.ServerWebExchange;


/**
 * Extension of ApiContext that can carry the ServerWebExchange.  Meant to be used only by the framework
 *
 * @author Paul Parrone
 */
class CarryingContext extends LushContext {
    @Getter
    ServerWebExchange exchange;

    void setExchange( ServerWebExchange exchange ) {
        this.exchange = exchange;
    }
}
