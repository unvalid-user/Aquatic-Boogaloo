package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.repository.projection.GamePlayersCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsByUser_IdAndGame_Id(Long userId, Long gameId);
    Optional<Player> findByUser_IdAndGame_Id(Long userId, Long gameId);

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

    int countByGame_IdAndStatus(Long id, PlayerStatus status);

    Page<Player> findByGame_Id(Long id, Pageable pageable);

    Optional<Player> findByIdAndGame_Id(Long playerId, Long gameId);
}
