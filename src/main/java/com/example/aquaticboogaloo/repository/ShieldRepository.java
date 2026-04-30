package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.field_objects.Shield;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShieldRepository extends JpaRepository<Shield, Long> {
    @Query("""
    SELECT case
        when count(s) > 0 then true
        else false
        end
    FROM Shield s
    WHERE s.game.id = :gameId
        AND s.action.locationX = :x
        AND s.action.locationY = :y
    """)
    boolean existsByCoordinate(Long gameId, int x, int y);

    @Query("""
    SELECT s
    FROM Shield s
    WHERE s.game.id = :gameId
        AND s.action.locationX = :x
        AND s.action.locationY = :y
    """)
    List<Shield> findByCoordinate(Long gameId, int x, int y);

    @Modifying
    @Query("""
    DELETE Shield s
    WHERE s.game.id = :gameId
        AND s.expirationTurn < :currentTurn
    """)
    int deleteExpiredShields(Long gameId, int currentTurn);
}