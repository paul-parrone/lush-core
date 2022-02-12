package com.px3j.lush.core.reactive.filters;

import com.px3j.lush.core.api.ApiContext;
import lombok.Getter;
import org.springframework.web.server.ServerWebExchange;


/**
 * Extension of ApiContext that can carry the ServerWebExchange.  Meant to be used only by the framework
 *
 * @author Paul Parrone
 */
public class CarryingApiContext extends ApiContext {
    @Getter
    ServerWebExchange exchange;

    void setExchange( ServerWebExchange exchange ) {
        this.exchange = exchange;
    }
}
