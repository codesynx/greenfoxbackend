package com.greenfox.backend.modules.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Service for Kaspi payment integration.
 * Currently uses emulation mode - to be replaced with actual Kaspi API integration.
 */
@Slf4j
@Service
public class KaspiService {

    @Value("${app.kaspi.merchant-id:GREENFOX_MERCHANT}")
    private String merchantId;

    @Value("${app.kaspi.callback-url:https://api.greenfox.kz/api/v1/bookings/callback}")
    private String callbackUrl;

    /**
     * Generate a Kaspi payment deep link for a booking.
     * This is emulation - actual integration requires Kaspi merchant API.
     *
     * @param bookingId Booking ID (used as order ID)
     * @param amount    Payment amount in KZT
     * @return Deep link URL or null if generation fails
     */
    public String generateDeepLink(UUID bookingId, BigDecimal amount) {
        try {
            // Emulated Kaspi deep link format
            // Real integration would use Kaspi merchant API to create payment
            String orderId = "GF-" + bookingId.toString().substring(0, 8).toUpperCase();
            String encodedCallback = URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8);
            
            // Format: kaspi://pay?merchant={merchantId}&order={orderId}&amount={amount}&callback={callback}
            String deepLink = String.format(
                    "kaspi://pay?merchant=%s&order=%s&amount=%s&callback=%s",
                    merchantId,
                    orderId,
                    amount.toBigInteger(),
                    encodedCallback
            );

            log.info("Generated Kaspi deep link for booking: {} amount: {}", bookingId, amount);
            return deepLink;
        } catch (Exception e) {
            log.error("Failed to generate Kaspi deep link for booking: {}", bookingId, e);
            return null;
        }
    }

    /**
     * Generate invoice ID for tracking.
     */
    public String generateInvoiceId(UUID bookingId) {
        return "GF-" + bookingId.toString().substring(0, 8).toUpperCase();
    }

    /**
     * Verify payment status with Kaspi.
     * Currently emulated - always returns true.
     *
     * @param invoiceId Invoice ID to check
     * @return true if payment is confirmed
     */
    public boolean verifyPayment(String invoiceId) {
        // TODO: Implement actual Kaspi payment verification API call
        log.info("Payment verification requested for invoice: {} (EMULATED - returning true)", invoiceId);
        return true;
    }
}

