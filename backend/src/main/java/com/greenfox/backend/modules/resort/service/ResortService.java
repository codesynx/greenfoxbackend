package com.greenfox.backend.modules.resort.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.common.exception.BadRequestException;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.common.service.StorageService;
import com.greenfox.backend.modules.resort.dto.*;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.mapper.ResortMapper;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for resort management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResortService {

    private final ResortRepository resortRepository;
    private final ResortMapper resortMapper;
    private final StorageService storageService;

    private static final int MAX_PHOTOS = 15;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    /**
     * Get paginated list of resorts with filters.
     * Public endpoint - sorted by promo first, then by rating.
     */
    @Transactional(readOnly = true)
    public PageResponse<ResortListResponse> getResorts(ResortFilterRequest filter, Pageable pageable) {
        Page<Resort> resorts = resortRepository.findAllWithDistance(filter, pageable);

        List<ResortListResponse> content = resorts.getContent().stream()
                .map(resortMapper::toListResponse)
                .toList();

        return PageResponse.from(resorts, content);
    }

    /**
     * Search resorts for AI module (returns full entities to avoid N+1 queries).
     */
    @Transactional(readOnly = true)
    public List<Resort> searchResortsForAi(ResortFilterRequest filter, Pageable pageable) {
        return resortRepository.findAllWithDistance(filter, pageable).getContent();
    }

    /**
     * Get resort by ID with full details.
     */
    @Transactional(readOnly = true)
    public ResortResponse getResortById(UUID id) {
        Resort resort = resortRepository.findByIdAndDeletedFalseAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", id));

        return resortMapper.toResponse(resort);
    }

    /**
     * Get resort entity by ID (internal use).
     */
    @Transactional(readOnly = true)
    public Resort getResortEntityById(UUID id) {
        return resortRepository.findByIdAndDeletedFalseAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", id));
    }

    /**
     * Get all unique cities for filtering.
     */
    @Transactional(readOnly = true)
    public List<String> getAllCities() {
        return resortRepository.findAllCities();
    }

    // ============== Admin Operations ==============

    /**
     * Create a new resort (Admin only).
     */
    @Transactional
    public ResortResponse createResort(CreateResortRequest request) {
        Resort resort = resortMapper.toEntity(request);
        resort.setPhotos(new ArrayList<>());

        Resort saved = resortRepository.save(resort);
        log.info("Resort created: {} ({})", saved.getName(), saved.getId());

        return resortMapper.toResponse(saved);
    }

    /**
     * Create a new resort with photos (Admin only, multipart/form-data).
     */
    @Transactional
    public ResortResponse createResortWithPhotos(
            String name, String city, String basePriceStr, String description,
            String latitudeStr, String longitudeStr, String address,
            String ratingStr, String reviewsCountStr, String maxGuestsStr, String amenitiesStr,
            List<MultipartFile> photos, String photoDescriptionsStr) throws IOException {
        
        // Parse base price
        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(basePriceStr);
            if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Base price must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid base price format");
        }

        // Build resort entity
        Resort resort = Resort.builder()
                .name(name.trim())
                .city(city.trim())
                .basePrice(basePrice)
                .description(description != null ? description.trim() : null)
                .latitude(parseBigDecimal(latitudeStr, "Latitude"))
                .longitude(parseBigDecimal(longitudeStr, "Longitude"))
                .address(address != null ? address.trim() : null)
                .rating(parseBigDecimal(ratingStr, "Rating"))
                .reviewsCount(parseInteger(reviewsCountStr, "Reviews count"))
                .maxGuests(parseInteger(maxGuestsStr, "Max guests"))
                .amenities(parseAmenities(amenitiesStr))
                .photos(new ArrayList<>())
                .promo(false)
                .active(true)
                .build();

        // Save resort first to get ID
        Resort saved = resortRepository.save(resort);

        // Parse photo descriptions
        List<String> descriptions = parsePhotoDescriptions(photoDescriptionsStr);

        // Upload and add photos
        if (photos != null && !photos.isEmpty()) {
            if (photos.size() > MAX_PHOTOS) {
                throw new BadRequestException(
                        String.format("Maximum %d photos allowed. Provided: %d", MAX_PHOTOS, photos.size())
                );
            }

            List<Resort.ResortPhoto> resortPhotos = new ArrayList<>();
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile file = photos.get(i);
                if (file == null || file.isEmpty()) {
                    continue;
                }

                // Validate file
                validatePhotoFile(file);

                // Upload to GCP
                String photoUrl = storageService.uploadFile(file, "resorts");
                if (photoUrl == null) {
                    throw new IOException("Failed to upload photo to cloud storage");
                }

                // Get description
                String description_text = (i < descriptions.size()) ? descriptions.get(i) : "";

                Resort.ResortPhoto photo = Resort.ResortPhoto.builder()
                        .url(photoUrl)
                        .description(description_text.trim())
                        .order(i)
                        .build();

                resortPhotos.add(photo);
            }

            saved.setPhotos(resortPhotos);
            saved = resortRepository.save(saved);
        }

        log.info("Resort created with {} photos: {} ({})", 
                saved.getPhotos().size(), saved.getName(), saved.getId());

        return resortMapper.toResponse(saved);
    }

    private BigDecimal parseBigDecimal(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException(String.format("Invalid %s format", fieldName));
        }
    }

    private Integer parseInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException(String.format("Invalid %s format", fieldName));
        }
    }

    private List<String> parseAmenities(String amenitiesStr) {
        if (amenitiesStr == null || amenitiesStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(amenitiesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    private List<String> parsePhotoDescriptions(String descriptionsStr) {
        if (descriptionsStr == null || descriptionsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(descriptionsStr.split("\\|"))
                .map(String::trim)
                .collect(java.util.stream.Collectors.toList());
    }

    private void validatePhotoFile(MultipartFile file) {
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
    }

    /**
     * Update an existing resort (Admin only).
     */
    @Transactional
    public ResortResponse updateResort(UUID id, UpdateResortRequest request) {
        Resort resort = resortRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", id));

        updateResortFields(resort, request);
        Resort saved = resortRepository.save(resort);
        log.info("Resort updated: {} ({})", saved.getName(), saved.getId());

        return resortMapper.toResponse(saved);
    }

    /**
     * Delete a resort (soft delete, Admin only).
     */
    @Transactional
    public void deleteResort(UUID id) {
        Resort resort = resortRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", id));

        resort.setDeleted(true);
        resortRepository.save(resort);
        log.info("Resort deleted: {} ({})", resort.getName(), id);
    }

    /**
     * Add photos to a resort (Admin only, max 15).
     */
    @Transactional
    public ResortResponse addPhotos(UUID id, List<ResortPhotoRequest> photoRequests) {
        Resort resort = resortRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", id));

        List<Resort.ResortPhoto> currentPhotos = resort.getPhotos() != null
                ? new ArrayList<>(resort.getPhotos())
                : new ArrayList<>();

        if (currentPhotos.size() + photoRequests.size() > MAX_PHOTOS) {
            throw new BadRequestException(
                    String.format("Maximum %d photos allowed. Current: %d, Adding: %d",
                            MAX_PHOTOS, currentPhotos.size(), photoRequests.size())
            );
        }

        int nextOrder = currentPhotos.stream()
                .mapToInt(Resort.ResortPhoto::getOrder)
                .max()
                .orElse(-1) + 1;

        for (ResortPhotoRequest photoRequest : photoRequests) {
            Resort.ResortPhoto photo = Resort.ResortPhoto.builder()
                    .url(photoRequest.getUrl())
                    .description(photoRequest.getDescription())
                    .order(photoRequest.getOrder() != null ? photoRequest.getOrder() : nextOrder++)
                    .build();
            currentPhotos.add(photo);
        }

        resort.setPhotos(currentPhotos);
        Resort saved = resortRepository.save(resort);
        log.info("Photos added to resort: {} ({})", saved.getName(), id);

        return resortMapper.toResponse(saved);
    }

    /**
     * Upload a photo file to a resort (Admin only).
     */
    @Transactional
    public PhotoUploadResponse uploadPhoto(UUID id, MultipartFile file, String description) throws IOException {
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

        // Get resort
        Resort resort = resortRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", id));

        // Check photo limit
        List<Resort.ResortPhoto> currentPhotos = resort.getPhotos() != null
                ? new ArrayList<>(resort.getPhotos())
                : new ArrayList<>();

        if (currentPhotos.size() >= MAX_PHOTOS) {
            throw new BadRequestException(
                    String.format("Maximum %d photos allowed. Current: %d", MAX_PHOTOS, currentPhotos.size())
            );
        }

        // Upload to GCP
        String photoUrl = storageService.uploadFile(file, "resorts");
        if (photoUrl == null) {
            throw new IOException("Failed to upload file to cloud storage");
        }

        // Determine order (next available)
        int nextOrder = currentPhotos.stream()
                .mapToInt(Resort.ResortPhoto::getOrder)
                .max()
                .orElse(-1) + 1;

        // Create photo entity
        Resort.ResortPhoto photo = Resort.ResortPhoto.builder()
                .url(photoUrl)
                .description(description != null ? description.trim() : "")
                .order(nextOrder)
                .build();

        // Add to resort
        currentPhotos.add(photo);
        resort.setPhotos(currentPhotos);
        resortRepository.save(resort);

        log.info("Photo uploaded to resort: {} ({}) - URL: {}", resort.getName(), id, photoUrl);

        return PhotoUploadResponse.builder()
                .url(photoUrl)
                .description(photo.getDescription())
                .order(photo.getOrder())
                .build();
    }

    /**
     * Remove a photo from a resort (Admin only).
     */
    @Transactional
    public ResortResponse removePhoto(UUID id, int photoOrder) {
        Resort resort = resortRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", id));

        List<Resort.ResortPhoto> photos = new ArrayList<>(resort.getPhotos());
        photos.removeIf(p -> p.getOrder() == photoOrder);
        resort.setPhotos(photos);

        Resort saved = resortRepository.save(resort);
        log.info("Photo removed from resort: {} ({})", saved.getName(), id);

        return resortMapper.toResponse(saved);
    }

    /**
     * Get all resorts for admin (including inactive).
     */
    @Transactional(readOnly = true)
    public PageResponse<ResortResponse> getResortsForAdmin(Pageable pageable) {
        Page<Resort> resorts = resortRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("deleted"), false),
                pageable
        );

        List<ResortResponse> content = resorts.getContent().stream()
                .map(resortMapper::toResponse)
                .toList();

        return PageResponse.from(resorts, content);
    }

    private void updateResortFields(Resort resort, UpdateResortRequest request) {
        if (request.getName() != null) {
            resort.setName(request.getName().trim());
        }
        if (request.getCity() != null) {
            resort.setCity(request.getCity().trim());
        }
        if (request.getDescription() != null) {
            resort.setDescription(request.getDescription());
        }
        if (request.getLatitude() != null) {
            resort.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            resort.setLongitude(request.getLongitude());
        }
        if (request.getAddress() != null) {
            resort.setAddress(request.getAddress());
        }
        if (request.getBasePrice() != null) {
            resort.setBasePrice(request.getBasePrice());
        }
        if (request.getRating() != null) {
            resort.setRating(request.getRating());
        }
        if (request.getReviewsCount() != null) {
            resort.setReviewsCount(request.getReviewsCount());
        }
        if (request.getAmenities() != null) {
            resort.setAmenities(request.getAmenities());
        }
        if (request.getMaxGuests() != null) {
            resort.setMaxGuests(request.getMaxGuests());
        }
        if (request.getActive() != null) {
            resort.setActive(request.getActive());
        }
    }
}
