package com.resexchange.app.security;

import com.resexchange.app.model.User;
import com.resexchange.app.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class REUserDetails implements UserDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(REUserDetails.class);
    private final User user;

    public REUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getMail() {
        return user.getMail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        user.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.name()))
        );

        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getMail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

