package com.example.aquaticboogaloo.dto.response.action;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TurnResultResponse {

    int turnNumber;
    List<ActionResponse> completedActions;
    List<ActionResponse> failedActions;
    List<AttackHitResponse> enemyHits;
    List<AttackResponse> attackResults;
}
