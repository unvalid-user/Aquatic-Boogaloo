package com.example.aquaticboogaloo.dto.mapper;

import com.example.aquaticboogaloo.config.MapStructConfig;
import com.example.aquaticboogaloo.dto.response.UserResponse;
import com.example.aquaticboogaloo.entity.User;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {
    UserResponse toResponse(User user);
}
