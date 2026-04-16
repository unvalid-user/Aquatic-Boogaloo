package com.example.aquaticboogaloo.entity.field_objects;

import com.example.aquaticboogaloo.entity.Action;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shields")
@NoArgsConstructor
@Getter
@Setter
public class Shield {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @Column(nullable = false)
    private int expirationTurn;

    // is_used?
}
