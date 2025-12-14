package com.greenfox.backend.modules.user.mapper;

import com.greenfox.backend.modules.user.dto.UserProfileResponse;
import com.greenfox.backend.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User entity to DTO conversion.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserProfileResponse toProfileResponse(User user);
}

