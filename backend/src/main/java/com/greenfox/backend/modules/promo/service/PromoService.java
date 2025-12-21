package com.greenfox.backend.modules.promo.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.common.exception.BadRequestException;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.common.service.StorageService;
import com.greenfox.backend.modules.promo.dto.CreatePromoRequest;
import com.greenfox.backend.modules.promo.dto.PromoResponse;
import com.greenfox.backend.modules.promo.entity.Promo;
import com.greenfox.backend.modules.promo.repository.PromoRepository;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.repository.ResortRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for promotion management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromoService {

    private final PromoRepository promoRepository;
    private final ResortRepository resortRepository;
    private final StorageService storageService;

    // File upload constants
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    /**
     * Get all currently active promos for homepage banners.
     */
    @Transactional(readOnly = true)
    public List<PromoResponse> getActivePromos() {
        List<Promo> promos = promoRepository.findActivePromos(LocalDate.now());
        return promos.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get active promo for a specific resort.
     */
    @Transactional(readOnly = true)
    public Optional<PromoResponse> getActivePromoForResort(UUID resortId) {
        return promoRepository.findActivePromoForResort(resortId, LocalDate.now())
                .map(this::toResponse);
    }

    /**
     * Calculate discounted price for a resort.
     */
    @Transactional(readOnly = true)
    public BigDecimal getDiscountedPrice(UUID resortId, BigDecimal originalPrice) {
        Optional<Promo> promo = promoRepository.findActivePromoForResort(resortId, LocalDate.now());
        if (promo.isPresent()) {
            BigDecimal discount = originalPrice
                    .multiply(BigDecimal.valueOf(promo.get().getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return originalPrice.subtract(discount);
        }
        return originalPrice;
    }

    // ============== Admin Operations ==============

    /**
     * Get all promos for admin.
     */
    @Transactional(readOnly = true)
    public PageResponse<PromoResponse> getAllPromos(Pageable pageable) {
        Page<Promo> promos = promoRepository.findByDeletedFalse(pageable);
        List<PromoResponse> content = promos.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.from(promos, content);
    }

    /**
     * Create a new promo (Admin only).
     */
    @Transactional
    public PromoResponse createPromo(CreatePromoRequest request) {
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Get resort
        Resort resort = resortRepository.findByIdAndDeletedFalse(request.getResortId())
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", request.getResortId()));

        Promo promo = Promo.builder()
                .resort(resort)
                .discountPercent(request.getDiscountPercent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .bannerImageUrl(request.getBannerImageUrl())
                .title(request.getTitle())
                .description(request.getDescription())
                .active(true)
                .build();

        Promo saved = promoRepository.save(promo);

        // Mark resort as promo if the promo is currently active
        if (saved.isCurrentlyActive()) {
            resort.setPromo(true);
            resortRepository.save(resort);
        }

        log.info("Promo created for resort: {} ({})", resort.getName(), saved.getId());
        return toResponse(saved);
    }

    /**
     * Delete a promo (Admin only).
     */
    @Transactional
    public void deletePromo(UUID id) {
        Promo promo = promoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo", "id", id));

        promo.setDeleted(true);
        promoRepository.save(promo);

        // Check if resort has other active promos
        List<Promo> activePromos = promoRepository.findActivePromos(LocalDate.now())
                .stream()
                .filter(p -> p.getResort().getId().equals(promo.getResort().getId()) && !p.getId().equals(id))
                .toList();

        if (activePromos.isEmpty()) {
            Resort resort = promo.getResort();
            resort.setPromo(false);
            resortRepository.save(resort);
        }

        log.info("Promo deleted: {}", id);
    }

    /**
     * Upload banner image for a promo (Admin only).
     */
    @Transactional
    public PromoResponse uploadBanner(UUID id, MultipartFile file) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    String.format("Invalid file type. Allowed types: %s", String.join(", ", ALLOWED_CONTENT_TYPES))
            );
        }

        // Get promo
        Promo promo = promoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo", "id", id));

        // Delete old banner if exists and is a cloud URL
        if (promo.getBannerImageUrl() != null && promo.getBannerImageUrl().startsWith("http")) {
            storageService.deleteFile(promo.getBannerImageUrl());
        }

        // Upload to GCP
        String bannerUrl = storageService.uploadFile(file, "promos");
        if (bannerUrl == null) {
            throw new IOException("Failed to upload file to cloud storage");
        }

        promo.setBannerImageUrl(bannerUrl);
        Promo saved = promoRepository.save(promo);

        log.info("Banner uploaded for promo: {} ({}) - URL: {}", saved.getId(), id, bannerUrl);

        return toResponse(saved);
    }

    private PromoResponse toResponse(Promo promo) {
        Resort resort = promo.getResort();
        BigDecimal promoPrice = resort.getBasePrice()
                .multiply(BigDecimal.valueOf(100 - promo.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return PromoResponse.builder()
                .id(promo.getId())
                .resortId(resort.getId())
                .resortName(resort.getName())
                .resortCity(resort.getCity())
                .discountPercent(promo.getDiscountPercent())
                .startDate(promo.getStartDate())
                .endDate(promo.getEndDate())
                .bannerImageUrl(promo.getBannerImageUrl())
                .title(promo.getTitle())
                .description(promo.getDescription())
                .originalPrice(resort.getBasePrice())
                .promoPrice(promoPrice)
                .active(promo.isCurrentlyActive())
                .build();
    }
}

