package com.px3j.lush.core.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Actor /*extends User*/ {
    private String username = "";
    private String password = "";
    private Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

    public void populateFrom( Actor other ) {
        BeanUtils.copyProperties( other, this );
    }
}
