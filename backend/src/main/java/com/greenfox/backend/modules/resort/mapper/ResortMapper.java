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
    ResortListResponse toListResponse(Resort resort);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "promo", constant = "false")
    @Mapping(target = "active", constant = "true")
    Resort toEntity(CreateResortRequest request);

    default String getMainPhotoUrl(Resort resort) {
        if (resort.getPhotos() == null || resort.getPhotos().isEmpty()) {
            return null;
        }
        return resort.getPhotos().stream()
                .filter(p -> p.getOrder() == 0)
                .findFirst()
                .map(Resort.ResortPhoto::getUrl)
                .orElse(resort.getPhotos().get(0).getUrl());
    }
}
