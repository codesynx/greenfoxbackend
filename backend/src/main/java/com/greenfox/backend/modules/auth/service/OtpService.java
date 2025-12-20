package com.greenfox.backend.modules.auth.service;

import com.greenfox.backend.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    
    // In-memory fallback storage when Redis is unavailable
    private final ConcurrentHashMap<String, String> memoryOtpStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> memoryThrottleStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean redisAvailable = true;

    @Value("${app.twilio.otp-expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.twilio.otp-length:4}")
    private int otpLength;

    @Value("${app.twilio.test-numbers:}")
    private String testNumbers;

    @Value("${app.twilio.test-otp-code:123456}")
    private String testOtpCode;

    private static final int THROTTLE_SECONDS = 60; // 1 minute between OTP requests
    
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("Test numbers configured: {}", testNumbers);
        log.info("Test OTP code: {}", testOtpCode);
    }

    /**
     * Generate and store a new OTP for the given phone number.
     * 
     * @return The generated OTP code
     */
    public String generateOtp(String phoneNumber) {
        String normalizedPhone = normalizePhone(phoneNumber);
        boolean isTestNumber = isTestNumber(normalizedPhone);
        
        // For test numbers, skip throttle and return test code immediately
        if (isTestNumber) {
            log.info("TEST MODE: Test number detected, always use code: {} for phone: {}", testOtpCode, maskPhone(phoneNumber));
            return testOtpCode;
        }
        
        // For real numbers, check throttle and generate OTP
        checkThrottle(phoneNumber);

        // Generate random OTP
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        String otpCode = otp.toString();

        // Store OTP in Redis with expiration (or fallback to memory)
        String key = OTP_PREFIX + normalizedPhone;
        String throttleKey = THROTTLE_PREFIX + normalizedPhone;
        
        try {
            if (redisAvailable) {
        redisTemplate.opsForValue().set(key, otpCode, Duration.ofMinutes(otpExpirationMinutes));
        redisTemplate.opsForValue().set(throttleKey, "1", Duration.ofSeconds(THROTTLE_SECONDS));
            } else {
                throw new Exception("Redis unavailable");
            }
        } catch (Exception e) {
            // Fallback to in-memory storage
            redisAvailable = false;
            log.warn("Redis unavailable, using in-memory storage for OTP");
            memoryOtpStore.put(key, otpCode);
            memoryThrottleStore.put(throttleKey, System.currentTimeMillis() + (THROTTLE_SECONDS * 1000L));
            
            // Schedule cleanup
            scheduler.schedule(() -> memoryOtpStore.remove(key), otpExpirationMinutes, TimeUnit.MINUTES);
            scheduler.schedule(() -> memoryThrottleStore.remove(throttleKey), THROTTLE_SECONDS, TimeUnit.SECONDS);
        }

        log.info("OTP generated for phone: {}", maskPhone(phoneNumber));
        return otpCode;
    }

    /**
     * Verify the OTP code for the given phone number.
     * 
     * @return true if OTP is valid
     */
    public boolean verifyOtp(String phoneNumber, String code) {
        String normalizedPhone = normalizePhone(phoneNumber);
        boolean isTestNumber = isTestNumber(normalizedPhone);
        
        // For test numbers, always accept the test code (no storage needed)
        if (isTestNumber) {
            if (testOtpCode.equals(code)) {
                log.info("TEST MODE: OTP verified for test number: {} with code: {}", maskPhone(phoneNumber), code);
                return true;
            } else {
                log.warn("TEST MODE: Invalid code for test number: {} (expected: {})", maskPhone(phoneNumber), testOtpCode);
                return false;
            }
        }
        
        // For real numbers, check stored OTP
        String key = OTP_PREFIX + normalizedPhone;
        String storedOtp = null;
        
        try {
            if (redisAvailable) {
                storedOtp = redisTemplate.opsForValue().get(key);
            } else {
                throw new Exception("Redis unavailable");
            }
        } catch (Exception e) {
            redisAvailable = false;
            storedOtp = memoryOtpStore.get(key);
        }

        if (storedOtp == null) {
            log.warn("OTP not found or expired for phone: {}", maskPhone(phoneNumber));
            return false;
        }

        if (storedOtp.equals(code)) {
            // Delete OTP after successful verification
            try {
                if (redisAvailable) {
            redisTemplate.delete(key);
                } else {
                    memoryOtpStore.remove(key);
                }
            } catch (Exception e) {
                memoryOtpStore.remove(key);
            }
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
        try {
            if (redisAvailable) {
        Long ttl = redisTemplate.getExpire(throttleKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl.intValue() : 0;
            } else {
                throw new Exception("Redis unavailable");
            }
        } catch (Exception e) {
            redisAvailable = false;
            Long throttleTime = memoryThrottleStore.get(throttleKey);
            if (throttleTime != null) {
                long remaining = (throttleTime - System.currentTimeMillis()) / 1000;
                return remaining > 0 ? (int) remaining : 0;
            }
            return 0;
        }
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

    /**
     * Check if the phone number is in the test numbers list.
     */
    public boolean isTestNumber(String phoneNumber) {
        String normalizedPhone = normalizePhone(phoneNumber);
        if (testNumbers == null || testNumbers.isBlank()) {
            log.debug("No test numbers configured");
            return false;
        }
        String[] testNumbersList = testNumbers.split(",");
        for (String testNumber : testNumbersList) {
            String normalizedTest = normalizePhone(testNumber.trim());
            log.debug("Comparing: '{}' with '{}'", normalizedPhone, normalizedTest);
            if (normalizedTest.equals(normalizedPhone)) {
                log.info("Test number matched: {}", maskPhone(phoneNumber));
                return true;
            }
        }
        log.debug("Phone number {} is not a test number", maskPhone(phoneNumber));
        return false;
    }
}
