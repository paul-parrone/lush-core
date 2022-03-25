package com.px3j.lush.endpoint.http.security.reactive;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.px3j.lush.core.model.Passport;
import com.px3j.lush.endpoint.http.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class LushSecurityContextRepository implements ServerSecurityContextRepository {
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        List<String> whoList = exchange.getRequest().getHeaders().get(Constants.WHO_HEADER_NAME);

        // Header isn't available, deny access...
        if(whoList == null || whoList.isEmpty()) {
            return Mono.empty();
        }

        // Header is an array, get the first element.
        final String whoAsJson = whoList.get(0);

        try {
            Gson gson = new GsonBuilder().create();

            final String decodedJson = new String(Base64.getDecoder().decode(whoAsJson.getBytes(StandardCharsets.UTF_8)));

            Passport passport = gson.fromJson( decodedJson, Passport.class);
            PassportAuthenticationToken authToken = new PassportAuthenticationToken(passport);
            authToken.setAuthenticated(true);

            return Mono.just( new SecurityContextImpl(authToken) );
        }
        catch (JsonSyntaxException e) {
            log.warn( "Invalid JSON in who header" );
            return Mono.empty();
        }
    }
}
