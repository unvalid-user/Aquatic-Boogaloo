package com.example.aquaticboogaloo.dto.filter;

import com.example.aquaticboogaloo.entity.enums.PlayerStatus;

public record PlayerFilter(
    Long gameId,
    PlayerStatus status
) {
}
