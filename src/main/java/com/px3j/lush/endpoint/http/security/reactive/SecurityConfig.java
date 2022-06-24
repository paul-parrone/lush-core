package com.px3j.lush.endpoint.http.security.reactive;


import com.px3j.lush.core.util.WithLushDebug;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig implements WithLushDebug {
    private final LushAuthenticationManager authenticationManager;
    private final LushSecurityContextRepository contextRepository;
    private final Logger lushDebug;

    @Autowired
    public SecurityConfig(LushAuthenticationManager authenticationManager, LushSecurityContextRepository contextRepository, Logger lushDebug) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.lushDebug = lushDebug;
    }

    @Value("${lush.security.protected-paths}")
    private List<String> protectedPaths;

    @Value("${lush.security.public-paths}")
    private List<String> publicPaths;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log( String.format("****") );
        log( String.format("Lush Security Configuration: ") );
        log( String.format("  - authenticationManager class: %s", authenticationManager.getClass().getName()) );
        log( String.format("  - contextRepository class: %s", contextRepository.getClass().getName()) );
        log( String.format("  - protected-paths: %s", protectedPaths.stream().collect(Collectors.joining(","))));
        log( String.format("  - public-paths: %s", publicPaths.stream().collect(Collectors.joining(","))));
        log( String.format("****") );

        return http
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()

                .authenticationManager( this.authenticationManager )
                .securityContextRepository( this.contextRepository )

                .authorizeExchange( exchanges -> {
                    exchanges.pathMatchers(HttpMethod.OPTIONS).permitAll();
                    publicPaths.forEach( p -> exchanges.pathMatchers(p).permitAll() );

                    protectedPaths.forEach( p -> exchanges.pathMatchers(p).authenticated() );
                })

                .build();
    }

    @Override
    public Logger getLushDebug() {
        return lushDebug;
    }
}

