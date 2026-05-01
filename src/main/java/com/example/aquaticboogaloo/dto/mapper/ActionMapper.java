package com.example.aquaticboogaloo.dto.mapper;

import com.example.aquaticboogaloo.config.MapStructConfig;
import com.example.aquaticboogaloo.dto.response.action.ActionResponse;
import com.example.aquaticboogaloo.entity.Action;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface ActionMapper {
    // TODO
    ActionResponse toResponse(Action action);
}
