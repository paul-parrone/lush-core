package com.px3j.lush.core.ticket;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementation of TicketUtil which is only to be used during development.  It simply converts a LushTicket to/from
 * JSON.
 * <br/><br/>
 * Only active if <b>developer</b> is one of the active Spring profiles.
 */
@Component
@Profile("clear-ticket")
@Slf4j( topic = "lush.core.debug")
public class ClearTicketUtil implements TicketUtil {
    public ClearTicketUtil() {
        log.debug( "Using ClearTicketUtil");
    }

    @Override
    public String encrypt(LushTicket ticket) {
        return new Gson().toJson(ticket);
    }

    @Override
    public LushTicket decrypt(String encryptedJson) {
        return new Gson().fromJson( encryptedJson, LushTicket.class );
    }
}
