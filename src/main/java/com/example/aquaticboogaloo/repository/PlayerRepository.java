package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsByUserIdAndGameId(Long userId, Long gameId);
    Optional<Player> findByUserIdAndGameId(Long userId, Long gameId);

    @Modifying
    @Query("""
    UPDATE Player p
    SET p.energy = p.energy - :amount
    WHERE p.id = :playerId
        AND p.energy >= :amount
    """)
    int subtractPlayerEnergy(Long playerId, int amount);

    @Modifying
    @Query("""
    UPDATE Player p
    SET p.points = p.points - :amount
    WHERE p.id = :playerId
    """)
    int subtractPlayerPoints(Long playerId, int amount);

    @Modifying
    @Query("""
    UPDATE Player p
    SET p.energy = p.energy + :amount
    WHERE p.id = :playerId
    """)
    int addPlayerEnergy(Long playerId, int amount);

    @Modifying
    @Query("""
    UPDATE Player p
    SET p.points = p.points + :amount
    WHERE p.id = :playerId
    """)
    int addPlayerPoints(Long playerId, int amount);
}
