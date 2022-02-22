package com.px3j.lush.service.endpoint.http.security.reactive;

import com.px3j.lush.core.security.Passport;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Map;

/**
 * Intergrate our Passport with Spring Security
 *
 * @author Paul Parrone
 */
public class PassportAuthenticationToken extends AbstractAuthenticationToken {
    private final Passport passport;

    public PassportAuthenticationToken(Passport passport) {
        super(passport.getAuthorities());
        this.passport = passport;
    }

    @Override
    public Object getCredentials() {
        return Map.of("username", passport.getUsername(), "password", passport.getPassword() );
    }

    @Override
    public Object getPrincipal() {
        return passport;
    }
}
