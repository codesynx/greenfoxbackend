package com.greenfox.backend.modules.auth.service;

import com.greenfox.backend.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for OTP generation, storage, and verification using Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String THROTTLE_PREFIX = "throttle:";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.twilio.otp-expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.twilio.otp-length:4}")
    private int otpLength;

    private static final int THROTTLE_SECONDS = 60; // 1 minute between OTP requests

    /**
     * Generate and store a new OTP for the given phone number.
     * 
     * @return The generated OTP code
     */
    public String generateOtp(String phoneNumber) {
        checkThrottle(phoneNumber);

        // Generate random OTP
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        String otpCode = otp.toString();

        // Store OTP in Redis with expiration
        String key = OTP_PREFIX + normalizePhone(phoneNumber);
        redisTemplate.opsForValue().set(key, otpCode, Duration.ofMinutes(otpExpirationMinutes));

        // Set throttle
        String throttleKey = THROTTLE_PREFIX + normalizePhone(phoneNumber);
        redisTemplate.opsForValue().set(throttleKey, "1", Duration.ofSeconds(THROTTLE_SECONDS));

        log.info("OTP generated for phone: {}", maskPhone(phoneNumber));
        return otpCode;
    }

    /**
     * Verify the OTP code for the given phone number.
     * 
     * @return true if OTP is valid
     */
    public boolean verifyOtp(String phoneNumber, String code) {
        String key = OTP_PREFIX + normalizePhone(phoneNumber);
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("OTP not found or expired for phone: {}", maskPhone(phoneNumber));
            return false;
        }

        if (storedOtp.equals(code)) {
            // Delete OTP after successful verification
            redisTemplate.delete(key);
            log.info("OTP verified successfully for phone: {}", maskPhone(phoneNumber));
            return true;
        }

        log.warn("Invalid OTP attempt for phone: {}", maskPhone(phoneNumber));
        return false;
    }

    /**
     * Check remaining time until a new OTP can be requested.
     * 
     * @return Seconds until next OTP can be requested, 0 if available now
     */
    public int getThrottleRemainingSeconds(String phoneNumber) {
        String throttleKey = THROTTLE_PREFIX + normalizePhone(phoneNumber);
        Long ttl = redisTemplate.getExpire(throttleKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl.intValue() : 0;
    }

    /**
     * Get OTP expiration time in seconds.
     */
    public int getOtpExpirationSeconds() {
        return otpExpirationMinutes * 60;
    }

    private void checkThrottle(String phoneNumber) {
        int remaining = getThrottleRemainingSeconds(phoneNumber);
        if (remaining > 0) {
            throw new BadRequestException(
                    String.format("Please wait %d seconds before requesting a new OTP", remaining)
            );
        }
    }

    private String normalizePhone(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9+]", "");
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "***";
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}
