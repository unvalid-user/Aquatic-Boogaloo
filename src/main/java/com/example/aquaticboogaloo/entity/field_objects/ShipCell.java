package com.example.aquaticboogaloo.entity.field_objects;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;


@Entity
@Table(name = "ship_cells")
@NoArgsConstructor
@Getter
@Setter
public class ShipCell implements Comparable<ShipCell>{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id", nullable = false)
    private Ship ship;

    @Column(nullable = false)
    private boolean isDestroyed = false;

    @Column(nullable = false)
    private int locationX;

    @Column(nullable = false)
    private int locationY;

    @Override
    public int compareTo(ShipCell sc) {
        return Comparator.comparingInt(ShipCell::getLocationX)
                .thenComparingInt(ShipCell::getLocationY)
                .compare(this, sc);
    }
}