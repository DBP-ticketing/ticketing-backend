package com.DBP.ticketing_backend.domain.auth.dto;

import com.DBP.ticketing_backend.domain.users.entity.Users;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UsersDetails implements UserDetails {

    @Getter
    private final Long userId;

    private final String email;
    private final String password;
    private final String role;

    @Getter
    private final String name;

    @Getter
    private final String phoneNumber;

    public UsersDetails(Users users) {
        this.userId = users.getUserId();
        this.email = users.getEmail();
        this.password = users.getPassword();
        this.role = users.getRole().getKey();
        this.name = users.getName();
        this.phoneNumber = users.getPhoneNumber();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
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
