package com.greenfox.backend.modules.user.service;

import com.greenfox.backend.common.exception.BadRequestException;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.common.service.StorageService;
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

import java.util.Base64;
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
    private final StorageService storageService;

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

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getAvatarUrl() != null) {
            String avatarData = request.getAvatarUrl();
            if (avatarData.startsWith("data:image")) {
                // Handle base64 upload
                try {
                    String[] parts = avatarData.split(",");
                    String metadata = parts[0];
                    String base64Content = parts[1];
                    String contentType = metadata.substring(metadata.indexOf(":") + 1, metadata.indexOf(";"));
                    String extension = contentType.substring(contentType.indexOf("/") + 1);
                    
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
                    
                    // Delete old avatar if exists and is a cloud URL
                    if (user.getAvatarUrl() != null && user.getAvatarUrl().startsWith("http")) {
                        storageService.deleteFile(user.getAvatarUrl());
                    }
                    
                    String newUrl = storageService.uploadBytes(
                            decodedBytes, 
                            "avatar." + extension, 
                            contentType, 
                            "avatars"
                    );
                    user.setAvatarUrl(newUrl);
                } catch (Exception e) {
                    log.error("Failed to process avatar base64 data", e);
                    throw new BadRequestException("Invalid avatar image data");
                }
            } else {
                // Just update the URL if it's already a link
                user.setAvatarUrl(avatarData);
            }
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
        String token = request.getDeviceToken().trim();
        if (token.length() < 10) {
            throw new BadRequestException("Invalid device token format");
        }
        
        User user = getUserById(principal.getId());
        user.setFcmToken(token);
        userRepository.save(user);
        log.info("FCM token registered for user: {}", principal.getId());
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

