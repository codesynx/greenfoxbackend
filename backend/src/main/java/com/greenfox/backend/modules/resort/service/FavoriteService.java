package com.greenfox.backend.modules.resort.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.common.exception.BadRequestException;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.modules.resort.dto.FavoriteResponse;
import com.greenfox.backend.modules.resort.dto.ResortListResponse;
import com.greenfox.backend.modules.resort.entity.Favorite;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.mapper.ResortMapper;
import com.greenfox.backend.modules.resort.repository.FavoriteRepository;
import com.greenfox.backend.modules.resort.repository.ResortRepository;
import com.greenfox.backend.modules.user.entity.User;
import com.greenfox.backend.modules.user.repository.UserRepository;
import com.greenfox.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing user favorite resorts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ResortRepository resortRepository;
    private final UserRepository userRepository;
    private final ResortMapper resortMapper;

    /**
     * Add a resort to favorites.
     */
    @Transactional
    public FavoriteResponse addFavorite(UserPrincipal principal, UUID resortId) {
        User user = userRepository.findByIdAndDeletedFalse(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Resort resort = resortRepository.findByIdAndDeletedFalseAndActiveTrue(resortId)
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", resortId));

        // Check if already favorited (including deleted ones for restoration)
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndResortId(user.getId(), resortId);
        
        Favorite saved;
        if (existingFavorite.isPresent()) {
            Favorite favorite = existingFavorite.get();
            if (!favorite.isDeleted()) {
                throw new BadRequestException("Resort is already in favorites");
            }
            // Restore deleted favorite
            favorite.setDeleted(false);
            saved = favoriteRepository.save(favorite);
            log.info("Resort {} restored in favorites by user {}", resortId, principal.getId());
        } else {
            // Create new favorite
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .resort(resort)
                    .build();
            saved = favoriteRepository.save(favorite);
            log.info("Resort {} added to favorites by user {}", resortId, principal.getId());
        }

        ResortListResponse resortResponse = resortMapper.toListResponse(resort);
        return FavoriteResponse.builder()
                .id(saved.getId())
                .resort(resortResponse)
                .favoritedAt(saved.getCreatedAt())
                .build();
    }

    /**
     * Remove a resort from favorites.
     */
    @Transactional
    public void removeFavorite(UserPrincipal principal, UUID resortId) {
        Favorite favorite = favoriteRepository
                .findByUserIdAndResortIdAndDeletedFalse(principal.getId(), resortId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite", "resortId", resortId));

        favorite.setDeleted(true);
        favoriteRepository.save(favorite);
        log.info("Resort {} removed from favorites by user {}", resortId, principal.getId());
    }

    /**
     * Get all favorite resorts for the current user.
     */
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getMyFavorites(UserPrincipal principal, Pageable pageable) {
        Page<Favorite> favorites = favoriteRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(
                principal.getId(), pageable);

        var content = favorites.getContent().stream()
                .map(favorite -> {
                    try {
                        ResortListResponse resortResponse = resortMapper.toListResponse(favorite.getResort());
                        return FavoriteResponse.builder()
                                .id(favorite.getId())
                                .resort(resortResponse)
                                .favoritedAt(favorite.getCreatedAt())
                                .build();
                    } catch (Exception e) {
                        log.error("Error mapping favorite {}: {}", favorite.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        return PageResponse.from(favorites, content);
    }

    /**
     * Check if a resort is favorited by the current user.
     */
    @Transactional(readOnly = true)
    public boolean isFavorited(UserPrincipal principal, UUID resortId) {
        return favoriteRepository.existsByUserIdAndResortIdAndDeletedFalse(principal.getId(), resortId);
    }
}

