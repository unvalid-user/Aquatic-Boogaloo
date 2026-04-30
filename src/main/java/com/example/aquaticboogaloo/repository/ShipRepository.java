package com.example.aquaticboogaloo.repository;

import com.example.aquaticboogaloo.entity.field_objects.Ship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {
    List<Ship> findByOwner_Game_Id(Long id);
}