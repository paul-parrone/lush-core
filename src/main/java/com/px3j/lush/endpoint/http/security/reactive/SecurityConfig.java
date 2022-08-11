package com.px3j.lush.endpoint.http.security.reactive;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j( topic = "lush.core.debug")
public class SecurityConfig {
    private final LushAuthenticationManager authenticationManager;
    private final LushSecurityContextRepository contextRepository;

    @Autowired
    public SecurityConfig(LushAuthenticationManager authenticationManager, LushSecurityContextRepository contextRepository) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
    }

    @Value("${lush.security.protected-paths}")
    private List<String> protectedPaths;

    @Value("${lush.security.public-paths}")
    private List<String> publicPaths;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info( "****" );
        log.info("Lush Security Configuration: ");
        log.info( String.format("  - authenticationManager class: %s", authenticationManager.getClass().getName()) );
        log.info( String.format("  - contextRepository class: %s", contextRepository.getClass().getName()) );
        log.info( String.format("  - protected-paths: %s", String.join(",", protectedPaths)));
        log.info( String.format("  - public-paths: %s", String.join(",", publicPaths)));
        log.info("****");

        return http
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()

                .authenticationManager( this.authenticationManager )
                .securityContextRepository( this.contextRepository )

                .authorizeExchange( exchanges -> {
                    exchanges.pathMatchers(HttpMethod.OPTIONS).permitAll();
                    // Require role: lush-monitor for actuator endpoints.
                    exchanges.pathMatchers("/actuator/**", "/health/**" ).hasAuthority("lush-monitor");

                    publicPaths.forEach( p -> exchanges.pathMatchers(p).permitAll() );

                    protectedPaths.forEach( p -> exchanges.pathMatchers(p).authenticated() );
                })

                .build();
    }
}

