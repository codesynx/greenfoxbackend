package com.greenfox.backend.modules.auth.service;

import com.greenfox.backend.common.exception.BadRequestException;
import com.greenfox.backend.common.exception.UnauthorizedException;
import com.greenfox.backend.modules.auth.dto.*;
import com.greenfox.backend.modules.user.entity.User;
import com.greenfox.backend.modules.user.repository.UserRepository;
import com.greenfox.backend.security.jwt.JwtProperties;
import com.greenfox.backend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service handling OTP flow and token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final TwilioService twilioService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * Send OTP to the given phone number.
     */
    public OtpResponse sendOtp(SendOtpRequest request) {
        String phoneNumber = normalizePhone(request.getPhoneNumber());

        // Generate OTP
        String otpCode = otpService.generateOtp(phoneNumber);

        // Send via Twilio
        boolean sent = twilioService.sendOtpSms(phoneNumber, otpCode);
        if (!sent) {
            throw new BadRequestException("Failed to send OTP. Please try again later.");
        }

        return OtpResponse.builder()
                .phoneNumber(maskPhone(phoneNumber))
                .expiresInSeconds(otpService.getOtpExpirationSeconds())
                .retryAfterSeconds(60)
                .message("OTP sent successfully")
                .build();
    }

    /**
     * Resend OTP to the given phone number.
     */
    public OtpResponse resendOtp(SendOtpRequest request) {
        String phoneNumber = normalizePhone(request.getPhoneNumber());

        int throttleRemaining = otpService.getThrottleRemainingSeconds(phoneNumber);
        if (throttleRemaining > 0) {
            return OtpResponse.builder()
                    .phoneNumber(maskPhone(phoneNumber))
                    .expiresInSeconds(otpService.getOtpExpirationSeconds())
                    .retryAfterSeconds(throttleRemaining)
                    .message(String.format("Please wait %d seconds before requesting a new OTP", throttleRemaining))
                    .build();
        }

        return sendOtp(request);
    }

    /**
     * Verify OTP and authenticate user.
     * Creates a new user if one doesn't exist.
     */
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String phoneNumber = normalizePhone(request.getPhoneNumber());

        // Verify OTP
        boolean isValid = otpService.verifyOtp(phoneNumber, request.getCode());
        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP code");
        }

        // Find or create user
        boolean isNewUser = false;
        User user = userRepository.findByPhoneNumberAndDeletedFalse(phoneNumber).orElse(null);

        if (user == null) {
            user = User.builder()
                    .phoneNumber(phoneNumber)
                    .role(User.UserRole.USER)
                    .active(true)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;
            log.info("New user registered: {}", maskPhone(phoneNumber));
        } else {
            log.info("User authenticated: {}", maskPhone(phoneNumber));
        }

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .role(user.getRole().name())
                .newUser(isNewUser)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpiration())
                .build();
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid token type");
        }

        // Get user from token
        var userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is disabled");
        }

        // Generate new tokens
        String newAccessToken = tokenProvider.generateAccessToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .role(user.getRole().name())
                .newUser(false)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpiration())
                .build();
    }

    private String normalizePhone(String phoneNumber) {
        String normalized = phoneNumber.replaceAll("[^0-9+]", "");
        // Ensure it starts with +
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }
        return normalized;
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "***";
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}

