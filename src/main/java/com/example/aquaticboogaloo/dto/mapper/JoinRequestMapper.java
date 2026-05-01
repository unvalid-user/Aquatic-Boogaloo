package com.example.aquaticboogaloo.dto.mapper;

import com.example.aquaticboogaloo.config.MapStructConfig;
import com.example.aquaticboogaloo.dto.response.JoinRequestResponse;
import com.example.aquaticboogaloo.entity.GameJoinRequest;
import org.mapstruct.Mapper;

@Mapper(
        config = MapStructConfig.class,
        uses = UserMapper.class
)
public interface JoinRequestMapper {

    JoinRequestResponse toResponse(GameJoinRequest request);
}
