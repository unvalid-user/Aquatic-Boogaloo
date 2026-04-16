package com.example.aquaticboogaloo.entity;

import com.example.aquaticboogaloo.entity.enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "games")
@NoArgsConstructor
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;
    private String description;
    private String avatarUrl;

    // if null - the game is public
    private String passwordHash;

    @Column(nullable = false)
    private boolean requestToJoin = true;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @ManyToMany
    @JoinTable(
            name = "game_moderators",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> moderators = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.NEW;

    private Integer fieldWidth;
    private Integer fieldHeight;

    @Column(nullable = false)
    private int currentTurn = 0;

    private Integer remainTurns;
    private Instant endsAt;
    private Instant nextTurnAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "game_ruleset_id", nullable = false)
    private GameRuleset ruleset;


    public void addPlayer(Player player) {
        player.setGame(this);
        players.add(player);
    }

    public void increaseTurn() {
        currentTurn++;
        remainTurns--;
    }
}
