package com.px3j.lush.core.config;

import com.google.gson.Gson;
import com.px3j.lush.core.model.LushAdvice;
import com.px3j.lush.core.ticket.LushTicket;
import com.px3j.lush.core.ticket.TicketUtil;
import com.px3j.lush.core.util.LushCore;
import com.px3j.lush.endpoint.jms.LushJmsTemplate;
import com.px3j.lush.endpoint.jms.LushMessageHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.Objects;

@Configuration
@Slf4j
public class LushBeanPostProcessor implements BeanPostProcessor {
    private final TicketUtil ticketUtil;
    private final Gson gson;

    private final Tracer tracer;

    @Autowired
    public LushBeanPostProcessor(TicketUtil ticketUtil, Gson gson, Tracer tracer) {
        this.ticketUtil = ticketUtil;
        this.gson = gson;
        this.tracer = tracer;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        applyLushJmsTemplate( bean, beanName );

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    private void applyLushJmsTemplate(Object bean, String beanName) {
        Arrays.stream(bean.getClass().getDeclaredFields()).filter(f -> f.getAnnotation(LushJmsTemplate.class) != null)
                .forEach(field -> {

                    try {
                        field.setAccessible(true);
                        Object fieldValue = field.get(bean);

                        if (!(fieldValue instanceof JmsTemplate)) {
                            log.warn("LushJmsTemplate applied to a field that is not instanceof JmsTemplate");
                            return;
                        }

                        JmsTemplate jmsTemplate = (JmsTemplate) fieldValue;
                        WrappedMessageConverter wrapped = new WrappedMessageConverter(jmsTemplate.getMessageConverter(), ticketUtil, gson, tracer);
                        jmsTemplate.setMessageConverter(wrapped);

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}

class WrappedMessageConverter implements MessageConverter {
    private final MessageConverter delegate;
    private final TicketUtil ticketUtil;
    private final Gson gson;
    private final Tracer tracer;

    @Autowired
    public WrappedMessageConverter(MessageConverter delegate, TicketUtil ticketUtil, Gson gson, Tracer tracer) {
        this.delegate = delegate;
        this.ticketUtil = ticketUtil;
        this.gson = gson;
        this.tracer = tracer;
    }

    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        Message message;
        LushTicket ticket = null;
        String traceId = Objects.requireNonNull(tracer.currentSpan()).context().traceId();
        traceId = traceId == null ? "" : traceId;

        LushAdvice advice = new LushAdvice( traceId );

        if( object instanceof LushMessageHolder) {
            LushMessageHolder holder = (LushMessageHolder) object;
            ticket = holder.ticket;
            object = holder.message;
        }

        message = delegate.toMessage( object, session );

        if( ticket != null ) {
            message.setStringProperty(LushCore.WHO_HEADER_NAME, ticketUtil.encrypt(ticket));
        }

        message.setStringProperty( LushCore.ADVICE_HEADER_NAME, gson.toJson(advice));

        return message;
    }

    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        return delegate.fromMessage(message);
    }
}
