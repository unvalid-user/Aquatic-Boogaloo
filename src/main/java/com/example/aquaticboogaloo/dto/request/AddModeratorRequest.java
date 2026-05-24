package com.example.aquaticboogaloo.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddModeratorRequest(
        @NotNull
        Long userId
) {
}
