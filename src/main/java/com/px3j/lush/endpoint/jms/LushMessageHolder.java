package com.px3j.lush.endpoint.jms;

import com.px3j.lush.core.ticket.Ticket;

public class LushMessageHolder {
    public final Ticket ticket;
    public final Object message;

    public LushMessageHolder(Ticket ticket, Object message) {
        this.ticket = ticket;
        this.message = message;
    }
}
