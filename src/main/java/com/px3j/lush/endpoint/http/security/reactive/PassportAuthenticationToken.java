package com.px3j.lush.endpoint.http.security.reactive;

import com.px3j.lush.core.passport.Passport;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
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
