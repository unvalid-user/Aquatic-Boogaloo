package com.example.aquaticboogaloo.dto.response;

import lombok.Data;

@Data
public class GameJoinResponse {
    JoinGameResult result;
    Long playerId;
    Long joinRequestId;

    public enum JoinGameResult{
        JOINED,
        PENDING_APPROVAL
    }
}
