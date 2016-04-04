package com.worth.ifs.commons.security;

import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Representation of an authenticated user, where its roles, name,etc can be obtained.
 */
public class UserAuthentication implements Authentication {


    private final transient User user;
    private boolean authenticated = true;

    public UserAuthentication(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (user == null) {
            return new ArrayList<>();
        }

        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Object getPrincipal() {
        return user.getName();
    }

    @Override
    public User getDetails() {
        return user;
    }

    @Override
    public Object getCredentials() {
        return user.getUid();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
