package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.GameRuleset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRulesetRepository extends JpaRepository<GameRuleset, Long> {
}
