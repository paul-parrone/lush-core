package com.px3j.lush.endpoint.jms;

import com.google.gson.Gson;
import com.px3j.lush.core.model.Advice;
import com.px3j.lush.core.ticket.Ticket;
import com.px3j.lush.core.ticket.TicketUtil;
import com.px3j.lush.core.util.LushCore;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Interface to be implemented by JMS receivers who wish to use Lush.  It provides simple access to the Lush Ticket
 */
public interface LushJmsReceiver {

    /**
     * Extract the ticket out of the give message.  This assumes that the message was populated by Lush from the sending
     * end.
     *
     * @param message The message to extract the Ticket from
     * @return The extracted Ticket.
     * @throws LushJmsException If the required header is missing from the message.
     */
    default Ticket getTicket(Message message) {
        TicketUtil ticketUtil = LushCore.getContext().getBean(TicketUtil.class);

        try {
            return ticketUtil.decrypt(message.getStringProperty(LushCore.WHO_HEADER_NAME));
        }
        catch (JMSException e) {
            throw new LushJmsException(e);
        }
    }

    default Advice getAdvice(Message message) {
        Gson gson = LushCore.getContext().getBean(Gson.class);

        try {
            String jsonAdvice = message.getStringProperty(LushCore.ADVICE_HEADER_NAME);
            return gson.fromJson( jsonAdvice, Advice.class );
        }
        catch (JMSException e) {
            throw new LushJmsException(e);
        }
    }
}
