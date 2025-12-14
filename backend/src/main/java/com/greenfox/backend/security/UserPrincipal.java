package com.greenfox.backend.security;

import com.greenfox.backend.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * UserDetails implementation for Spring Security.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String phoneNumber;
    private final User.UserRole role;
    private final boolean active;

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return null; // OTP-based auth, no password
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public boolean isAdmin() {
        return role == User.UserRole.ADMIN;
    }
}

