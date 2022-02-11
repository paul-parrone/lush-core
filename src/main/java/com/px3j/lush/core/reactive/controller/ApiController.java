package com.px3j.lush.core.reactive.controller;

import com.px3j.lush.core.api.ApiResponse;
import com.px3j.lush.core.api.ApiException;
import org.springframework.web.server.ServerWebExchange;

import static com.px3j.lush.core.api.ApiConstants.RESPONSE_HEADER_NAME;

public interface ApiController {
    default ApiResponse extractApiContext(ServerWebExchange exchange ) {
        ApiResponse context = (ApiResponse)exchange.getAttributes().get(RESPONSE_HEADER_NAME);
        if( null == context ) throw new ApiException( "590", "Exchange is missing the api-context attribute" );

        return context;

    }

}
