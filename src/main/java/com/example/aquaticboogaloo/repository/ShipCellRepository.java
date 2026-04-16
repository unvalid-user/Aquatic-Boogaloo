package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipCellRepository extends JpaRepository<ShipCell, Long> {
    @Query("""
    SELECT case
        when count(sc) > 0 then true
        else false
        end
    FROM ShipCell sc
    WHERE sc.ship.owner.game.id = :gameId
        AND sc.locationX = :x
        AND sc.locationY = :y
    """)
    boolean existsByCoordinate(Long gameId, int x, int y);

    @Query("""
    SELECT sc
    FROM ShipCell sc
    WHERE sc.ship.owner.game.id = :gameId
        AND sc.locationX = :x
        AND sc.locationY = :y
    """)
    Optional<ShipCell> findByCoordinate(Long gameId, int x, int y);

    @Query("""
    SELECT count(sc)
    FROM ShipCell sc
    WHERE sc.locationX between :x - :r and :x + :r
        AND sc.locationY between :y - :r and :y + :r
        AND sc.ship.owner.game.id = :gameId
    """)
    int getNumberOfShipCellsInRadius(Long gameId, int x, int y, int r);
}