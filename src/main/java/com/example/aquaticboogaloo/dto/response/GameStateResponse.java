package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.dto.response.action.*;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.enums.ActionType;

import java.util.List;
import java.util.Map;

public record GameStateResponse(
        GameResponse game,
        UserInGameInfo membership,
        GameRulesetResponse rules,
        GameFieldResponse field,
        List<KnownCellResponse> knownCells,
        List<ActionResponse> actions,
        Map<ActionType, Integer> bonuses,
        TurnResultResponse results,
        List<PlayerResponse> players
) {}
