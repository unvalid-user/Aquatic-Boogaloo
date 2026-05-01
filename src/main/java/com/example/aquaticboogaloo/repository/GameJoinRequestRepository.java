package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.GameJoinRequest;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.entity.enums.JoinRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameJoinRequestRepository extends JpaRepository<GameJoinRequest, Long> {
    boolean existsByUserIdAndGameId(Long id, Long id1);

    Page<GameJoinRequest> findByGame_Id(Long gameId, Pageable pageable);

    Optional<GameJoinRequest> findByIdAndGame_Id(Long id, Long gameId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT jr
    FROM GameJoinRequest jr
    WHERE jr.id = :joinRequestId
        AND jr.game.id = :gameId
    """)
    Optional<GameJoinRequest> findByIdAndGameIdForUpdate(Long joinRequestId, Long gameId);

    @Modifying
    @Query("""
    UPDATE GameJoinRequest jr
    SET jr.status = :newStatus, jr.rejectedBy = :user
    WHERE jr.id = :joinRequestId
        AND jr.status = :oldStatus
    """)
    int updateJoinRequest(Long joinRequestId, User user, JoinRequestStatus newStatus, JoinRequestStatus oldStatus);
}
