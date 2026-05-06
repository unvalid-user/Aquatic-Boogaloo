package com.example.aquaticboogaloo.dto.response.action;

import com.example.aquaticboogaloo.entity.enums.AttackStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttackResponse {
    Long id;
    private Long playerId;
    private int locationX;
    private int locationY;
    AttackStatus status;
    AttackHitResponse hit;
}
