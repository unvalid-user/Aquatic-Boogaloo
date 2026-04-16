package com.example.aquaticboogaloo.entity;

import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.enums.BonusType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_bonuses")
@NoArgsConstructor
@Getter
@Setter
public class BonusAction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType type;

    @Column(nullable = false)
    private int quantity;
}
