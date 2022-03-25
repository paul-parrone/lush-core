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

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private LushAuthenticationManager authenticationManager;
    private LushSecurityContextRepository contextRepository;

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
        log.debug( String.format("****") );
        log.debug( String.format("Lush Security Configuration: ") );
        log.debug( String.format("  - authenticationManager class: %s", authenticationManager.getClass().getName()) );
        log.debug( String.format("  - contextRepository class: %s", contextRepository.getClass().getName()) );
        log.debug( String.format("  - protected-paths: %s",protectedPaths.stream().collect(Collectors.joining(","))));
        log.debug( String.format("  - public-paths: %s",publicPaths.stream().collect(Collectors.joining(","))));
        log.debug( String.format("****") );

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

/*
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User
                .withUsername("user")
                .password("password")
//                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
*/
}

