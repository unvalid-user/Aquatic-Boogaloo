package com.example.aquaticboogaloo.dto.filter;

import com.example.aquaticboogaloo.entity.enums.JoinRequestStatus;
import jakarta.validation.constraints.NotNull;

public record JoinRequestFilter (
        String username,
        @NotNull
        Long gameId,
        JoinRequestStatus status
) {
}
