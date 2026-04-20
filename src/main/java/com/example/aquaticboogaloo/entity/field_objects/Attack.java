package com.example.aquaticboogaloo.entity.field_objects;

import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.enums.AttackStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attacks")
@NoArgsConstructor
@Getter
@Setter
public class Attack {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttackStatus status;
}
