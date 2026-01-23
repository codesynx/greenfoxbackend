package com.greenfox.backend.modules.resort.mapper;

import com.greenfox.backend.modules.resort.dto.CreateResortRequest;
import com.greenfox.backend.modules.resort.dto.ResortListResponse;
import com.greenfox.backend.modules.resort.dto.ResortResponse;
import com.greenfox.backend.modules.resort.entity.Resort;
import org.mapstruct.*;

/**
 * MapStruct mapper for Resort entity to DTO conversion.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ResortMapper {

    @Mapping(target = "promoDiscountPercent", ignore = true)
    @Mapping(target = "promoPrice", ignore = true)
    ResortResponse toResponse(Resort resort);

    @Mapping(target = "mainPhotoUrl", expression = "java(getMainPhotoUrl(resort))")
    @Mapping(target = "promoPrice", ignore = true)
    @Mapping(target = "distanceKm", expression = "java(resort.getDistanceKm())")
    ResortListResponse toListResponse(Resort resort);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "promo", constant = "false")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "totalRooms", constant = "1")
    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "houseRules", ignore = true)
    @Mapping(target = "highlights", ignore = true)
    @Mapping(target = "neighborhoodDescription", ignore = true)
    @Mapping(target = "transportDescription", ignore = true)
    @Mapping(target = "nearbyAttractions", ignore = true)
    @Mapping(target = "cancellationPolicy", ignore = true)
    @Mapping(target = "categorizedAmenities", ignore = true)
    Resort toEntity(CreateResortRequest request);

    default String getMainPhotoUrl(Resort resort) {
        if (resort.getPhotos() == null || resort.getPhotos().isEmpty()) {
            return null;
        }
        return resort.getPhotos().stream()
                .filter(p -> p != null && p.getOrder() == 0)
                .findFirst()
                .map(Resort.ResortPhoto::getUrl)
                .orElseGet(() -> resort.getPhotos().stream()
                        .filter(p -> p != null)
                        .findFirst()
                        .map(Resort.ResortPhoto::getUrl)
                        .orElse(null));
    }
}
