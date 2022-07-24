package com.px3j.lush.core.ticket;

/**
 * Contains common LushTicket related operations.
 */
public interface TicketUtil {

    String encrypt( LushTicket ticket);

    LushTicket decrypt(final String encryptedJson );
}
