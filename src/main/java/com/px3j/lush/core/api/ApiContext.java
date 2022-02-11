package com.px3j.lush.core.api;

import lombok.*;
import org.springframework.web.server.ServerWebExchange;

@NoArgsConstructor
@Data
public class ApiContext {
    private ApiResponse response;
    private String requestKey;
}