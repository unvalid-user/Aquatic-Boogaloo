package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.field_objects.Scan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanRepository extends JpaRepository<Scan, Long> {
    @Modifying
    @Query("""
    DELETE Scan s
    WHERE s.action.actor.game.id = :gameId
        AND s.expirationTurn < :currentTurn
    """)
    int deleteExpiredScans(Long gameId, int currentTurn);
}