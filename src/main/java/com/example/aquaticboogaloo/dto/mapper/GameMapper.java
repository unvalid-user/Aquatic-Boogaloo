package com.example.aquaticboogaloo.dto.mapper;

import com.example.aquaticboogaloo.config.MapStructConfig;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.GameRulesetResponse;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.GameRuleset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = UserMapper.class)
public interface GameMapper {
    @Mapping(
            target = "requiresPasswordToJoin",
            expression = "java(game.getPasswordHash() != null)"
    )
    GameResponse toResponse(Game game);

    GameRulesetResponse toResponse(GameRuleset ruleset);
}
