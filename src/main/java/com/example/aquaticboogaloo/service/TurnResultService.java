package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.ActionMapper;
import com.example.aquaticboogaloo.dto.mapper.AttackMapper;
import com.example.aquaticboogaloo.dto.response.action.TurnResultResponse;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TurnResultService {

    private final ActionService actionService;
    private final PlayerService playerService;
    private final ActionMapper actionMapper;
    private final AttackMapper attackMapper;
    private final AttackService attackService;


    public TurnResultResponse getTurnResults(Long gameId, Long userId, Integer turn) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);

        // TODO: check game status?

        if (turn == null) {
            turn = player.getGame().getCurrentTurn() - 1;
        }

        var actions = actionService.getActionsByPlayerIdAndTurn(player.getId(), turn);
        var attacks = attackService.getAttacksByPlayerIdAndTurn(player.getId(), turn);
        var enemyHits = attackService.getAttackHitsByObjectOwnerIdAndTurn(player.getId(), turn);

        TurnResultResponse response = new TurnResultResponse();
        response.setTurnNumber(turn);
        response.setFailedActions(
                actions.stream()
                        .filter(action -> action.getStatus() == ActionStatus.FAILED)
                        .map(actionMapper::toResponse)
                        .toList()
        );
        response.setAttackResults(
                attacks.stream()
                        .map(attackMapper::toResponse)
                        .toList()
        );
        response.setEnemyHits(
                enemyHits.stream()
                        .map(attackMapper::toResponse)
                        .toList()
        );

        return response;
    }
}
