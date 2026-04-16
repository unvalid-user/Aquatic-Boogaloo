package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.BonusAction;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BonusActionRepository extends JpaRepository<BonusAction, Long> {
    @Modifying
    @Query("""
    UPDATE BonusAction b
    SET b.quantity = b.quantity - :amount
    WHERE b.player.id = :playerId
        AND b.type = :actionType
        AND b.quantity >= :amount
    """)
    int subtractBonus(Long playerId, ActionType actionType, int amount);

    @Modifying
    @Query("""
    UPDATE BonusAction b
    SET b.quantity = b.quantity + :amount
    WHERE b.player.id = :playerId
        AND b.type = :actionType
    """)
    int addBonus(Long playerId, ActionType actionType, int amount);
}
