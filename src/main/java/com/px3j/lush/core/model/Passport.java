package com.px3j.lush.core.model;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class Passport {
    private String id;
    private String username = "";
    private String password = "";
    private Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

    public Passport() {
        this.id = null;
    }

    public Passport(String username, String password, Collection<SimpleGrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public void populateFrom( Passport other ) {
        BeanUtils.copyProperties( other, this );
    }
}
