package com.px3j.lush.endpoint.http.security.reactive;

import com.google.gson.JsonSyntaxException;
import com.px3j.lush.core.ticket.Ticket;
import com.px3j.lush.core.ticket.TicketUtil;
import com.px3j.lush.endpoint.http.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class LushSecurityContextRepository implements ServerSecurityContextRepository {
    private final TicketUtil ticketUtil;

    @Autowired
    public LushSecurityContextRepository(TicketUtil ticketUtil) {
        this.ticketUtil = ticketUtil;
    }

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
            Ticket ticket = ticketUtil.decrypt(whoAsJson);

            TicketAuthenticationToken authToken = new TicketAuthenticationToken(ticket);
            authToken.setAuthenticated(true);

            return Mono.just( new SecurityContextImpl(authToken) );
        }
        catch (JsonSyntaxException e) {
            log.warn( "Invalid JSON in who header" );
            return Mono.empty();
        }
    }
}
