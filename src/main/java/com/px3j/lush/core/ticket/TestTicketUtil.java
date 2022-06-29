package com.px3j.lush.core.ticket;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementation of TicketUtil which is only to be used during development.  It simply converts a Ticket to/from
 * JSON.
 * <br/><br/>
 * Only active if <b>developer</b> is one of the active Spring profiles.
 */
@Component
@Profile("developer")
@Slf4j( topic = "lush.core.debug")
public class TestTicketUtil implements TicketUtil {
    public TestTicketUtil() {
        log.debug( "Using no-op ticket util");
    }

    @Override
    public String encrypt(Ticket ticket) {
        return new Gson().toJson(ticket);
    }

    @Override
    public Ticket decrypt(String encryptedJson) {
        return new Gson().fromJson( encryptedJson, Ticket.class );
    }
}
