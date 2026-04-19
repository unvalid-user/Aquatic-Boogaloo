package com.example.aquaticboogaloo.dto.filter;

import com.example.aquaticboogaloo.entity.enums.GameStatus;

public record GameFilter(
        String search,
        GameStatus status,
        Boolean requiresPasswordToJoin
) {
}
