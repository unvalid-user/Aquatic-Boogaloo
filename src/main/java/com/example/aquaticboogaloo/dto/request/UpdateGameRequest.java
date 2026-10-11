package com.example.aquaticboogaloo.dto.request;

import java.time.Duration;
import java.time.Instant;

public record UpdateGameRequest (
        String title,
        String description,
        String avatarUrl,
        String password,
        Boolean requestToJoin,
        Boolean forceNextTurn,
        Integer remainTurns,
        Instant endsAt,
        Instant turnAdvanceAt,
        Duration turnDuration
) {
}
