package com.px3j.lush.core.config;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.px3j.lush.core.util.YamlPropertySourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties( prefix = "yaml" )
@PropertySource( value = "classpath:lush-config.yml", factory = YamlPropertySourceFactory.class)
@Slf4j( topic = "lush.core.debug")
public class LushCoreConfig {

    public LushCoreConfig() {
        log.debug( "Lush :: LushCoreConfig initialization" );
    }

    @Bean
    BaggageField lushUserNameField() {
        return BaggageField.create("lush-user-name");
    }

    @Bean
    CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(lushUserNameField())
                        .flushOnUpdate()
                        .build())
                .build();
    }

    @Bean
    Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }
}
