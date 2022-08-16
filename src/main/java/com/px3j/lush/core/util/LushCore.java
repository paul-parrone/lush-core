package com.px3j.lush.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class LushCore implements ApplicationContextAware {
    public static final String TICKET_HEADER_NAME = "x-lush-ticket";
    public static final String ADVICE_HEADER_NAME = "x-lush-advice";

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LushCore.context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
