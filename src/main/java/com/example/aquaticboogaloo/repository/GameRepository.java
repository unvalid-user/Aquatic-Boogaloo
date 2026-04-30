package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {
    Optional<Game> findByIdAndHostUser_Id(Long gameId, Long userId);

    @Query("""
    SELECT g
    FROM Game g
    WHERE g.id = :gameId
        AND (
            g.hostUser.id = :userId
            OR EXISTS (
                SELECT 1
                FROM g.moderators m
                WHERE m.id = :userId
            )
        )
    """)
    Optional<Game> findGameByIdAndHostIdOrModeratorId(Long gameId, Long userId);

    @Modifying
    @Query("""
    UPDATE Game g
    SET g.status = :newStatus
    WHERE g.id = :gameId
        AND g.status = :currentStatus
    """)
    int updateGameStatus(Long gameId, GameStatus currentStatus, GameStatus newStatus);


    @Query("""
    SELECT g.id
    FROM Game g
    WHERE g.turnAdvanceAt <= :now
        AND g.status = :status
    """)
    List<Long> findGameIdsWithExpiredTurn(Instant now, GameStatus status);
}
