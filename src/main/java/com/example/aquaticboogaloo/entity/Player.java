package com.example.aquaticboogaloo.entity;

import com.example.aquaticboogaloo.entity.enums.BonusType;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
@NoArgsConstructor
@Getter
@Setter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    private PlayerStatus status;

    private int energy;
    private int points;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BonusAction> bonuses = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ship> ships = new ArrayList<>();

    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Action> actions = new ArrayList<>();


    public void addShip(Ship ship) {
        ship.setOwner(this);
        ships.add(ship);
    }

    public void addBonus(BonusAction bonus) {
        bonus.setPlayer(this);
        bonuses.add(bonus);
    }

    public void addAction(Action action) {
        action.setActor(this);
        actions.add(action);
    }

    public void addPoints(int amount) {
        points += amount;
    }
    public void removePoints(int amount) {
        points -= amount;
    }
}
