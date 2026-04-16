package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.entity.enums.GameStatus;
import lombok.Data;

// TODO: players count

@Data
public class GameResponse {
    private long id;
    private String title;
    private String description;
    private String avatarUrl;
    private GameStatus status;
    private boolean requiresPasswordToJoin;
    private boolean requestToJoin;
}
