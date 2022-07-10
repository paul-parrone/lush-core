package com.px3j.lush.endpoint.http.security.reactive;

import com.px3j.lush.core.ticket.Ticket;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Map;

/**
 * Intergrate our Ticket with Spring Security
 *
 * @author Paul Parrone
 */
public class TicketAuthenticationToken extends AbstractAuthenticationToken {
    private final Ticket ticket;

    public TicketAuthenticationToken(Ticket ticket) {
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
