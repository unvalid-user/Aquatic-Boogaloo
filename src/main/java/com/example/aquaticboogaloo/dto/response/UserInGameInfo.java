package com.example.aquaticboogaloo.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInGameInfo {
    boolean isHost;
    boolean isModerator;
    PlayerResponse player;
}
