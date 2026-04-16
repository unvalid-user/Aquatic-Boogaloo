package com.example.aquaticboogaloo.entity.field_objects;

import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.enums.ShipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "ships")
@NoArgsConstructor
@Getter
@Setter
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_player_id", nullable = false)
    private Player owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipStatus status = ShipStatus.INTACT;

    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipCell> shipCells = new ArrayList<>();


    public void addCell(ShipCell cell) {
        shipCells.add(cell);
        cell.setShip(this);
    }
}
