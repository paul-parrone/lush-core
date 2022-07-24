package com.px3j.lush.endpoint.http.security.reactive;

import com.px3j.lush.core.ticket.LushTicket;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Map;

/**
 * Intergrate our LushTicket with Spring Security
 *
 * @author Paul Parrone
 */
public class TicketAuthenticationToken extends AbstractAuthenticationToken {
    private final LushTicket ticket;

    public TicketAuthenticationToken(LushTicket ticket) {
        super(ticket.getAuthorities());
        this.ticket = ticket;
    }

    @Override
    public Object getCredentials() {
        return Map.of("username", ticket.getUsername(), "password", ticket.getPassword() );
    }

    @Override
    public Object getPrincipal() {
        return ticket;
    }
}
