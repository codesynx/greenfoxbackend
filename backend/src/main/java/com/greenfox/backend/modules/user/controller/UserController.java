package com.greenfox.backend.modules.user.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.modules.user.dto.DeviceTokenRequest;
import com.greenfox.backend.modules.user.dto.UpdateProfileRequest;
import com.greenfox.backend.modules.user.dto.UserProfileResponse;
import com.greenfox.backend.modules.user.service.UserService;
import com.greenfox.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * User profile management controller.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(
            summary = "Get Profile",
            description = "Get current user's profile information"
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userService.getProfile(principal);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping("/profile")
    @Operation(
            summary = "Update Profile",
            description = "Update user's name and/or avatar. Phone number cannot be changed."
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(principal, request);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
    }

    @PostMapping("/device-token")
    @Operation(
            summary = "Save Device Token",
            description = "Register FCM device token for push notifications"
    )
    public ResponseEntity<ApiResponse<Void>> saveDeviceToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeviceTokenRequest request) {
        userService.saveDeviceToken(principal, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Device token saved successfully"));
    }
}

