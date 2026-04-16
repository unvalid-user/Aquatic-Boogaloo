package com.example.aquaticboogaloo.entity;

import com.example.aquaticboogaloo.entity.enums.AttackHitImpact;
import com.example.aquaticboogaloo.entity.enums.FieldObjectType;
import com.example.aquaticboogaloo.entity.field_objects.Attack;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attack_hits")
@NoArgsConstructor
@Getter
@Setter
public class AttackHit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "attack_id", nullable = false)
    private Attack attack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldObjectType objectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttackHitImpact hitImpact;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "object_owner_player_id")
    private Player objectOwner;
}
