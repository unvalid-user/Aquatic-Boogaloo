package com.example.aquaticboogaloo.entity.field_objects;

import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.Game;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scans")
@NoArgsConstructor
@Getter
@Setter
public class Scan implements ActionObject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private int expirationTurn;

    @Column(nullable = false)
    private int shipCellsNumber;

    @Column(nullable = false)
    private int minesNumber;
}
