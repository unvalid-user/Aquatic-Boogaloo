package com.example.aquaticboogaloo.dto.response.action;

import com.example.aquaticboogaloo.entity.enums.AttackHitImpact;
import com.example.aquaticboogaloo.entity.enums.FieldObjectType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttackHitResponse {
    Long id;
    FieldObjectType objectType;
    Long hitBackShipCellId;
    AttackHitImpact hitImpact;
    Long objectOwnerPlayerId;
}
