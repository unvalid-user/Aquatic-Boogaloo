package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.GameJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameJoinRequestRepository extends JpaRepository<GameJoinRequest, Long> {
    boolean existsByUserIdAndGameId(Long id, Long id1);
}
