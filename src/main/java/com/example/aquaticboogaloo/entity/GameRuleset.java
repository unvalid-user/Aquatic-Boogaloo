package com.example.aquaticboogaloo.entity;

import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.enums.BonusType;
import com.example.aquaticboogaloo.entity.enums.ShipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: versioning or unmodifiable rules during the game ???
//  add Default ruleset

@Entity
@Table(name = "game_rulesets")
@NoArgsConstructor
@Getter
@Setter
public class GameRuleset {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    private int visionRadius = 1;
    private int defaultPlayerEnergy = 5;
    private int defaultPlayerPoints = 0;

    // TODO
//    private int shieldLifetimeInTurns = 1;

    private int attackEnergyCost = 2;
    private int shieldEnergyCost = 3;
    private int mineEnergyCost = 1;
    private int scanEnergyCost = 5;
    private int scanRadius = 1;
//    private boolean canScanMines = true;

    private int turnSurviveBonus = 2;
    private int shipHitBonus = 1;
    private int shipHitPenalty = 1;
    private int mineHitBackBonus = 0;
    private int mineHitBackPenalty = 0;
    private int shipDestroyBonus = 1;

    // TODO: this is NOT OK
    @Column(name = "k1_ship_quantity")
    private int k1ShipsQuantity = 4;
    @Enumerated(EnumType.STRING)
    @Column(name = "k1_bonus_type", nullable = false)
    private BonusType k1BonusType = BonusType.POINTS;
    @Column(name = "k1_bounus_quantity")
    private int k1BonusQuantity = 1;

    @Column(name = "k2_ship_quantity")
    private int k2ShipsQuantity = 3;
    @Enumerated(EnumType.STRING)
    @Column(name = "k2_bonus_type", nullable = false)
    private BonusType k2BonusType = BonusType.ENERGY;
    @Column(name = "k2_bounus_quantity")
    private int k2BonusQuantity = 1;

    @Column(name = "k3_ship_quantity")
    private int k3ShipsQuantity = 2;
    @Enumerated(EnumType.STRING)
    @Column(name = "k3_bonus_type", nullable = false)
    private BonusType k3BonusType = BonusType.FREE_ATTACK;
    @Column(name = "k3_bounus_quantity")
    private int k3BonusQuantity = 1;

    @Column(name = "k4_ship_quantity")
    private int k4ShipsQuantity = 1;
    @Enumerated(EnumType.STRING)
    @Column(name = "k4_bonus_type", nullable = false)
    private BonusType k4BonusType = BonusType.FREE_SCAN;
    @Column(name = "k4_bounus_quantity")
    private int k4BonusQuantity = 1;

    private BonusType skipTurnBonusType = BonusType.POINTS;
    private int skipTurnBonusQuantity = 3;


    public ShipRule getShipRule(ShipType type) {
        return switch (type) {
            case K1 -> new ShipRule(
                    ShipType.K1,
                    k1ShipsQuantity,
                    k1BonusType,
                    k1BonusQuantity
            );
            case K2 -> new ShipRule(
                    ShipType.K2,
                    k2ShipsQuantity,
                    k2BonusType,
                    k2BonusQuantity
            );
            case K3 -> new ShipRule(
                    ShipType.K3,
                    k3ShipsQuantity,
                    k3BonusType,
                    k3BonusQuantity
            );
            case K4 -> new ShipRule(
                    ShipType.K4,
                    k4ShipsQuantity,
                    k4BonusType,
                    k4BonusQuantity
            );
        };
    }

    public int getEnergyCost(ActionType actionType) {
        return switch (actionType) {
            case ATTACK -> getAttackEnergyCost();
            case PLACE_SHIELD -> getShieldEnergyCost();
            case PLACE_MINE -> getMineEnergyCost();
            case SCAN -> getScanEnergyCost();
        };
    }
}
