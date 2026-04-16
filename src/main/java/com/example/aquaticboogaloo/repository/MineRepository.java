package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.field_objects.Mine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MineRepository extends JpaRepository<Mine, Long> {
  @Query("""
    SELECT case
        when count(m) > 0 then true
        else false
        end
    FROM Mine m
    WHERE m.action.actor.game.id = :gameId
        AND m.action.locationX = :x
        AND m.action.locationY = :y
    """)
  boolean existsByCoordinate(Long gameId, int x, int y);

  @Query("""
    SELECT m
    FROM Mine m
    WHERE m.action.actor.game.id = :gameId
        AND m.action.locationX = :x
        AND m.action.locationY = :y
    """)
  Optional<Mine> findByCoordinate(Long gameId, int x, int y);

  @Query("""
    SELECT count(m)
    FROM Mine m
    WHERE m.action.locationX between :x - :r and :x + :r
        AND m.action.locationY between :y - :r and :y + :r
        AND m.action.actor.game.id = :gameId
    """)
  int getNumberOfMinesInRadius(Long gameId, int x, int y, int r);
}