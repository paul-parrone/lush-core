package com.px3j.lush.core;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import com.px3j.lush.core.util.YamlPropertySourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties( prefix = "yaml" )
@PropertySource( value = "classpath:lush-config.yml", factory = YamlPropertySourceFactory.class)
public class LushCoreConfig {
    private static final Logger lushDebugLogger = LoggerFactory.getLogger("lush.core.debug" );

    public LushCoreConfig() {
        lushDebugLogger.debug( "Lush :: LushCoreConfig initialization" );
    }

    @Value( "${lush.exists}" ) String testProp;
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
    Logger lushDebug() {
        return lushDebugLogger;
    }
}
