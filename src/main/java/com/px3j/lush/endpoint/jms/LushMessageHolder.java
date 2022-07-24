package com.px3j.lush.endpoint.jms;

import com.px3j.lush.core.ticket.LushTicket;

public class LushMessageHolder {
    public final LushTicket ticket;
    public final Object message;

    public LushMessageHolder(LushTicket ticket, Object message) {
        this.ticket = ticket;
        this.message = message;
    }
}
