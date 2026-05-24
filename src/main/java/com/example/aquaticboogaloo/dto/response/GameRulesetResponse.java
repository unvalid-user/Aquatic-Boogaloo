package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.entity.enums.BonusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRulesetResponse {
    private Long id;
    private int visionRadius;
    private int defaultPlayerEnergy;
    private int defaultPlayerPoints;
    private int attackEnergyCost;
    private int shieldEnergyCost;
    private int mineEnergyCost;
    private int scanEnergyCost;
    private int scanRadius;
    private int turnSurviveBonus;
    private int shipHitBonus;
    private int shipHitPenalty;
    private int mineHitBackBonus;
    private int mineHitBackPenalty;
    private int shipDestroyBonus;
    private int k1ShipsQuantity;
    private BonusType k1BonusType;
    private int k1BonusQuantity;
    private int k2ShipsQuantity;
    private BonusType k2BonusType;
    private int k2BonusQuantity;
    private int k3ShipsQuantity;
    private BonusType k3BonusType;
    private int k3BonusQuantity;
    private int k4ShipsQuantity;
    private BonusType k4BonusType;
    private int k4BonusQuantity;
    private BonusType skipTurnBonusType;
    private int skipTurnBonusQuantity;
}
