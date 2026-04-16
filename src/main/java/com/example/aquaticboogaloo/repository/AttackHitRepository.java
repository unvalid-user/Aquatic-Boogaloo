package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.AttackHit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttackHitRepository extends JpaRepository<AttackHit, Long> {
}