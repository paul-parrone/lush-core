package com.px3j.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;

/**
 * This is a simple Spring Boot application showing how to use Lush in your application.
 *
 * @author Paul Parrone
 */
@Slf4j
@SpringBootApplication
@ComponentScan( {
        "com.px3j.lush.core",
        "com.px3j.lush.endpoint.http",
//        "com.px3j.lush.endpoint.jms",
        "com.px3j.lush.endpoint.websocket",
        "com.px3j.example"
})
@EnableJms
public class LushExampleServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(LushExampleServiceApp.class, args);
    }

    // TODO: JMS coming soon...
/*
    @Bean
    public JmsListenerContainerFactory<?> myFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer)
    {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
*/
}
