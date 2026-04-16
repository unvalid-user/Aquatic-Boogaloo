package com.example.aquaticboogaloo.entity;

import com.example.aquaticboogaloo.entity.enums.BonusType;
import com.example.aquaticboogaloo.entity.enums.ShipType;

public record ShipRule (
        ShipType type,
        int quantity,
        BonusType bonusType,
        int bonusQuantity
) {
}
