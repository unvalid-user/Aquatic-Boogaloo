package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.entity.enums.GameStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class GameResponse {
    private long id;
    private String title;
    private String description;
    private String avatarUrl;
    private UserResponse hostUser;
    private GameStatus status;
    private int playersCount;
    private boolean requiresPasswordToJoin;
    private boolean requestToJoin;
    private Integer currentTurn;
    private Integer remainTurns;
    private Instant endsAt;
    private Instant nextTurnAt;
}
