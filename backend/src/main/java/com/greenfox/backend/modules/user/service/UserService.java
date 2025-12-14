package com.greenfox.backend.modules.user.service;

import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.modules.user.dto.DeviceTokenRequest;
import com.greenfox.backend.modules.user.dto.UpdateProfileRequest;
import com.greenfox.backend.modules.user.dto.UserProfileResponse;
import com.greenfox.backend.modules.user.entity.User;
import com.greenfox.backend.modules.user.mapper.UserMapper;
import com.greenfox.backend.modules.user.repository.UserRepository;
import com.greenfox.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for user profile management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Get current user's profile.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UserPrincipal principal) {
        User user = getUserById(principal.getId());
        return userMapper.toProfileResponse(user);
    }

    /**
     * Update current user's profile.
     * Note: Phone number cannot be changed.
     */
    @Transactional
    public UserProfileResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        User user = getUserById(principal.getId());

        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }

        if (request.getAvatarUrl() != null) {
            // TODO: Handle avatar upload via GCP if base64 data is provided
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User savedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", principal.getId());

        return userMapper.toProfileResponse(savedUser);
    }

    /**
     * Save FCM device token for push notifications.
     */
    @Transactional
    public void saveDeviceToken(UserPrincipal principal, DeviceTokenRequest request) {
        User user = getUserById(principal.getId());
        user.setFcmToken(request.getDeviceToken());
        userRepository.save(user);
        log.info("Device token saved for user: {}", principal.getId());
    }

    /**
     * Get user by ID (internal use).
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Get user by phone number (internal use).
     */
    @Transactional(readOnly = true)
    public User getUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumberAndDeletedFalse(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", phoneNumber));
    }
}

