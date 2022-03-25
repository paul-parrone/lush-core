package com.px3j.lush.endpoint.http.reactive;

import com.px3j.lush.core.model.LushContext;
import lombok.Getter;
import org.springframework.web.server.ServerWebExchange;


/**
 * Extension of LushContext that can carry the ServerWebExchange.  This class is meant to be used internally by Lush only.
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
