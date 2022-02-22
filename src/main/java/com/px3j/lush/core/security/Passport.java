package com.px3j.lush.core.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@Document(collection = "passports")
public class Passport {
    @Id
    @Getter @Setter
    private String id;
    @Getter @Setter
    private String username = "";
    @Getter @Setter
    private String password = "";
    @Getter @Setter
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
