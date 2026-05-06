package com.example.aquaticboogaloo.dto.mapper;

import com.example.aquaticboogaloo.config.MapStructConfig;
import com.example.aquaticboogaloo.dto.response.action.ActionResponse;
import com.example.aquaticboogaloo.entity.Action;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ActionMapper {
    @Mapping(target = "playerId", source = "actor.id")
    ActionResponse toResponse(Action action);
}
