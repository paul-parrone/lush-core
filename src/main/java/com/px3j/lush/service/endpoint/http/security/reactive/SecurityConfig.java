package com.px3j.lush.service.endpoint.http.security.reactive;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository contextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()

                .authenticationManager( this.authenticationManager )
                .securityContextRepository( this.contextRepository )

                .authorizeExchange( exchanges -> {
                    exchanges.pathMatchers("/oauth/sim/**" ).permitAll();
                    exchanges.pathMatchers("/cats/**").authenticated();
                })

/*
                .authenticationManager(new ReactiveAuthenticationManager() {
                    @Override
                    public Mono<Authentication> authenticate(Authentication authentication) {
                        authentication.setAuthenticated( true );
                        return Mono.just(authentication);
                    }
                })
*/
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

