package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.field_objects.Attack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttackRepository extends JpaRepository<Attack, Long> {

    List<Attack> findByAction_Actor_Id(Long id);

    List<Attack> findByAction_Actor_IdAndAction_CreatedAtTurn(Long id, int createdAtTurn);

    List<Attack> findByAction_CreatedAtTurn(int createdAtTurn);
}