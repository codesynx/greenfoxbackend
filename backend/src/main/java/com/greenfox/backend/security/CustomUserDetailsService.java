package com.greenfox.backend.security;

import com.greenfox.backend.modules.user.entity.User;
import com.greenfox.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Custom UserDetailsService for loading user information.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumberAndDeletedFalse(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with phone: " + phoneNumber));
        
        return UserPrincipal.from(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id));
        
        return UserPrincipal.from(user);
    }
}

