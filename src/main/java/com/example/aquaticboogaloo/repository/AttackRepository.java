package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.field_objects.Attack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttackRepository extends JpaRepository<Attack, Long> {
}