package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerResponse {
    private Long id;
    private UserResponse user;
    private Long gameId;
    private PlayerStatus status;
    private int points;
    private int energy;
}
