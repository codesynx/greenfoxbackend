package com.greenfox.backend.modules.user.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.modules.user.dto.DeviceTokenRequest;
import com.greenfox.backend.modules.user.dto.UpdateProfileRequest;
import com.greenfox.backend.modules.user.dto.UserProfileResponse;
import com.greenfox.backend.modules.user.service.UserService;
import com.greenfox.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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
            description = "Update user's name and/or email. Phone number cannot be changed. " +
                         "Avatar upload is handled via separate endpoint: POST /api/v1/users/profile/avatar/upload"
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

    @PostMapping(value = "/profile/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Upload Profile Avatar",
            description = "Upload a profile avatar image using multipart/form-data. " +
                         "The file will be automatically uploaded to GCP Cloud Storage and set as the user's avatar. " +
                         "Supported formats: JPEG, PNG, WebP. Maximum file size: 10MB. " +
                         "In Swagger UI, click 'Try it out' and use the 'Choose File' button to select an image."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Multipart form data with image file",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "object", description = "Form data with image file")
            )
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(
                    description = "Image file to upload (JPEG, PNG, or WebP). In Swagger UI, use the file picker button.",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam("file") MultipartFile file) {
        try {
            UserProfileResponse profile = userService.uploadAvatar(principal, file);
            return ResponseEntity.ok(ApiResponse.success(profile, "Avatar uploaded successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPLOAD_ERROR", "Failed to upload avatar: " + e.getMessage()));
        }
    }
}

