package com.example.aquaticboogaloo.entity;

import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "actions",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {
                "actor_player_id",
                "created_at_turn",
                "location_x",
                "location_y"
        }
    )
)
@NoArgsConstructor
@Getter
@Setter
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_player_id", nullable = false)
    private Player actor;

    // if null - bonus action have been used
    private Integer energyCost;

    @Column(name = "created_at_turn", nullable = false)
    private int createdAtTurn;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionStatus status = ActionStatus.PLANNED;

    private String failCauseMessage;


    public boolean isBonus() {
        return energyCost == null;
    }

    public void setFail(String cause) {
        setStatus(ActionStatus.FAILED);
        setFailCauseMessage(cause);
    }
}
