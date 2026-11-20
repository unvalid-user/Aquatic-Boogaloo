package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.ActionMapper;
import com.example.aquaticboogaloo.dto.mapper.AttackMapper;
import com.example.aquaticboogaloo.dto.response.action.ActionResponse;
import com.example.aquaticboogaloo.dto.response.action.TurnResultResponse;
import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.AttackHit;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.field_objects.Attack;
import com.example.aquaticboogaloo.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TurnResultService {

    private final ActionService actionService;
    private final PlayerService playerService;
    private final ActionMapper actionMapper;
    private final AttackMapper attackMapper;
    private final AttackService attackService;
    private final GameService gameService;
    private final ActionRepository actionRepository;

    public TurnResultResponse getTurnResultsForModeratorView(Long gameId, Long userId, Integer turn) {
        Game game = gameService.findGameByIdAndHostIdOrModeratorId(gameId, userId);

        return getTurnResults(game, null, turn);
    }
    public TurnResultResponse getTurnResultsForPlayerView(Long gameId, Long userId, Integer turn) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);

        return getTurnResults(player.getGame(), player.getId(), turn);
    }

    public TurnResultResponse getTurnResults(Game game, Long playerId, Integer turn) {
        // TODO: check game status?

        if (turn == null) {
            turn = game.getCurrentTurn() - 1;
        }

        var actions = getActions(playerId, turn).stream()
            .filter(a -> a.getActor().getGame().getId().equals(game.getId()))
            .toList();
        var attacks = getAttackResults(playerId, turn).stream()
            .filter(a -> a.getAction().getActor().getGame().getId().equals(game.getId()))
            .toList();
        var enemyHits = getEnemyHits(playerId, turn);

        Map<Long, Action> completed = new LinkedHashMap<>();
        actions.stream()
                .filter(a -> a.getStatus() == ActionStatus.COMPLETED)
                .forEach(a -> completed.put(a.getId(), a));
        if (playerId != null) {
            actionRepository.findByActor_IdAndResultAtTurn(playerId, turn)
                    .forEach(a -> completed.put(a.getId(), a));
        }

        TurnResultResponse response = new TurnResultResponse();
        response.setTurnNumber(turn);
        response.setCompletedActions(
                completed.values().stream()
                        .map(actionMapper::toResponse)
                        .toList()
        );
        response.setFailedActions(
                actions.stream()
                        .filter(action -> action.getStatus() == ActionStatus.FAILED)
                        .map(actionMapper::toResponse)
                        .toList()
        );
        response.setAttackResults(attacks.stream().map(attackMapper::toResponse).toList());
        response.setEnemyHits(enemyHits.stream().map(attackMapper::toResponse).toList());

        return response;
    }

    private List<Action> getActions(Long playerId, int turn) {
        return playerId == null
                ? actionService.findActionsByTurn(turn)
                : actionService.findActionsByPlayerIdAndTurn(playerId, turn);
    }
    private List<Attack> getAttackResults(Long playerId, int turn) {
        return playerId == null
                ? attackService.findAttacksByTurn(turn)
                : attackService.findAttacksByPlayerIdAndTurn(playerId, turn);
    }
    private List<AttackHit> getEnemyHits(Long playerId, int turn) {
        return playerId == null
                ? List.of()
                : attackService.findAttackHitsByObjectOwnerIdAndTurn(playerId, turn);
    }
}
