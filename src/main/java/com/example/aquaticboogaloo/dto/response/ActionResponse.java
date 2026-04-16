package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import lombok.Data;

@Data
public class ActionResponse {
    private Long id;
    private Long playerId;
    private Integer energyCost;
    private int createdAtTurn;
    private int locationX;
    private int locationY;
    ActionType type;
    ActionStatus status;
    private String failCauseMessage;

}
