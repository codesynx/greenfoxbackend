package com.greenfox.backend.modules.auth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMS via Twilio.
 */
@Slf4j
@Service
public class TwilioService {

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.phone-number}")
    private String fromPhoneNumber;

    @Value("${app.twilio.test-numbers:}")
    private String testNumbers;

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            if (!accountSid.startsWith("your-") && !authToken.startsWith("your-")) {
                Twilio.init(accountSid, authToken);
                initialized = true;
                log.info("Twilio initialized successfully");
            } else {
                log.warn("Twilio not configured - using mock mode for development");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Twilio: {}", e.getMessage());
        }
    }

    /**
     * Send OTP SMS to the specified phone number.
     *
     * @param toPhoneNumber Recipient phone number
     * @param otpCode       The OTP code to send
     * @return true if SMS was sent successfully
     */
    public boolean sendOtpSms(String toPhoneNumber, String otpCode) {
        String messageBody = String.format("Your GreenFox verification code is: %s. Valid for 5 minutes.", otpCode);

        // Skip real SMS for test numbers
        if (isTestNumber(toPhoneNumber)) {
            log.info("TEST MODE: Skipping SMS for test number: {}. Code: {}", toPhoneNumber, otpCode);
            return true;
        }

        if (!initialized) {
            // Mock mode for development
            log.info("MOCK SMS to {}: {}", toPhoneNumber, messageBody);
            return true;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            log.info("SMS sent successfully. SID: {}", message.getSid());
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Check if Twilio is properly configured.
     */
    public boolean isConfigured() {
        return initialized;
    }

    /**
     * Check if the phone number is in the test numbers list.
     */
    private boolean isTestNumber(String phoneNumber) {
        if (testNumbers == null || testNumbers.isBlank()) {
            return false;
        }
        String normalized = phoneNumber.replaceAll("[^0-9+]", "");
        String[] testNumbersList = testNumbers.split(",");
        for (String testNumber : testNumbersList) {
            String normalizedTest = testNumber.trim().replaceAll("[^0-9+]", "");
            if (normalizedTest.equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}

