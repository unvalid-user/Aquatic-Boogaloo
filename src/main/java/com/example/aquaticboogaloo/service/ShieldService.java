package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.repository.ShieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShieldService {

    private final ShieldRepository shieldRepository;

    public int deleteExpiredShields(Game game) {
        return shieldRepository.deleteExpiredShields(game.getId(), game.getCurrentTurn());
    }
}
