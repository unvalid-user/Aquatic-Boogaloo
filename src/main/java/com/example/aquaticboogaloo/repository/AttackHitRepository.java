package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.AttackHit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttackHitRepository extends JpaRepository<AttackHit, Long> {

    List<AttackHit> findByObjectOwner_IdAndAttack_Action_CreatedAtTurn(Long id, int createdAtTurn);
}