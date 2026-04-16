package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    @Query("""
    SELECT a
    FROM Action a
    WHERE a.actor.game.id = :gameId
        AND a.createdAtTurn = :turn
        AND a.status = :status
        AND a.type = :type
    """)
    List<Action> findByTypeAndStatus(Long gameId, int turn, ActionStatus status, ActionType type);

}