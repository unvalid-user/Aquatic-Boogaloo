package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.repository.ShipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipService {

    private final ShipRepository shipRepository;

    public List<Ship> findGameShips(Long gameId) {
        return shipRepository.findByOwner_Game_Id(gameId);
    }
}
