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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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

        // Note: Avatar upload is handled via separate endpoint: POST /api/v1/users/profile/avatar/upload

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

    /**
     * Upload avatar image file.
     */
    @Transactional
    public UserProfileResponse uploadAvatar(UserPrincipal principal, MultipartFile file) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        // Validate file size (10MB)
        long maxFileSize = 10 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size of 10 MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        List<String> allowedTypes = List.of("image/jpeg", "image/jpg", "image/png", "image/webp");
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid file type. Allowed types: JPEG, PNG, WebP");
        }

        User user = getUserById(principal.getId());

        // Delete old avatar if exists and is a cloud URL
        if (user.getAvatarUrl() != null && user.getAvatarUrl().startsWith("http")) {
            storageService.deleteFile(user.getAvatarUrl());
        }

        // Upload to GCP
        String avatarUrl = storageService.uploadFile(file, "avatars");
        
        if (avatarUrl == null) {
            throw new IOException("Failed to upload file to cloud storage");
        }

        user.setAvatarUrl(avatarUrl);
        User savedUser = userRepository.save(user);
        log.info("Avatar uploaded for user: {}", principal.getId());

        return userMapper.toProfileResponse(savedUser);
    }
}
