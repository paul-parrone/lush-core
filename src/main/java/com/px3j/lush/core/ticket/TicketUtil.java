package com.px3j.lush.core.ticket;

/**
 * Contains common Ticket related operations.
 */
public interface TicketUtil {

    String encrypt( Ticket ticket);

    Ticket decrypt(final String encryptedJson );
}
