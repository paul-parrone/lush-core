package com.px3j.lush.service.endpoint.http.security.reactive;

import com.px3j.lush.core.security.Actor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Document(collection = "passports")
public class Passport implements Authentication {
    @Id
    @Getter @Setter
    private String id;
    @Getter @Setter
    private final Actor actor;
    @Getter @Setter
    private boolean isAuthenticated;

    public Passport() {
        this.id = null;
        this.actor = null;
        this.isAuthenticated = false;
    }

    public Passport(Actor actor) {
        this.actor = actor;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return actor.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return actor;
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return actor.getUsername();
    }
}
