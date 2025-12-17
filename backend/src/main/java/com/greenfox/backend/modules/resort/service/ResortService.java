package com.greenfox.backend.modules.resort.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.common.exception.BadRequestException;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
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

import java.util.ArrayList;
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

    private static final int MAX_PHOTOS = 15;

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

