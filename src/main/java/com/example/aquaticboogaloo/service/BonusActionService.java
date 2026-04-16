package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.repository.BonusActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BonusActionService {
    private final BonusActionRepository bonusActionRepository;



    // TODO: rename mb
    //  + BonusAction -> unique (player_id, type)
    public boolean removeBonus(Player player, ActionType actionType) {
        int rows = bonusActionRepository.subtractBonus(player.getId(), actionType, 1);

        return rows > 0;
    }

    public boolean addBonus(Player player, ActionType actionType) {
        int rows = bonusActionRepository.addBonus(player.getId(), actionType, 1);

        return rows > 0;
    }
}
