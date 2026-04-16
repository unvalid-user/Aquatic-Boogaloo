package com.example.aquaticboogaloo.entity.field_objects;

import com.example.aquaticboogaloo.entity.Action;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scans")
@NoArgsConstructor
@Getter
@Setter
public class Scan {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @Column(nullable = false)
    private int expirationTurn;

    @Column(nullable = false)
    private int shipCellsNumber;

    @Column(nullable = false)
    private int minesNumber;
}
