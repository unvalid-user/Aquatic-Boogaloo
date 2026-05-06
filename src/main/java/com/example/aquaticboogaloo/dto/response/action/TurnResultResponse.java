package com.example.aquaticboogaloo.dto.response.action;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TurnResultResponse {
    int turnNumber;
    List<ActionResponse> failedActions;
    List<AttackHitResponse> enemyHits;
    List<AttackResponse> attackResults;
}
