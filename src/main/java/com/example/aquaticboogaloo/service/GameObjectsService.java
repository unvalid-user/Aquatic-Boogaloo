package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.repository.ScanRepository;
import com.example.aquaticboogaloo.repository.ShieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameObjectsService {

    private final ShieldRepository shieldRepository;
    private final ScanRepository scanRepository;

    public int deleteExpiredShields(Game game) {
        return shieldRepository.deleteExpiredShields(game.getId(), game.getCurrentTurn());
    }

    public int deleteExpiredScans(Game game) {
        return scanRepository.deleteExpiredScans(game.getId(), game.getCurrentTurn());
    }
}
