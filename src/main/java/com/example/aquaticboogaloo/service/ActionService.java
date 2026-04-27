package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.request.ActionRequest;
import com.example.aquaticboogaloo.dto.response.ActionCreationResponse;
import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.event.PlayerCommitedActionsEvent;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.ActionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.*;
import static com.example.aquaticboogaloo.util.EntityConst.ACTION;
import static com.example.aquaticboogaloo.util.EntityConst.ID;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final PlayerService playerService;
    private final ActionValidationService actionValidationService;
    private final ActionRepository actionRepository;
    private final BonusActionService bonusActionService;

    private final ApplicationEventPublisher eventPublisher;


    public ActionCreationResponse createActions(Long gameId, Long userId, List<ActionRequest> actionRequests) {
        if (actionRequests == null || actionRequests.isEmpty())
            throw new BadRequestException(EMPTY_REQUEST_BODY);

        Player player = getPlayerAndValidate(gameId, userId);

        return actionValidationService.createActions(player, actionRequests);
    }

    @Transactional
    public void cancelAction(Long gameId, Long actionId, Long userId) {
        Player player = getPlayerAndValidate(gameId, userId);
        Action action = findActionById(actionId);

        if (!Objects.equals(action.getActor().getId(), player.getId()))
            throw new AccessDeniedException();

        if (action.getStatus() != ActionStatus.PLANNED)
            throw new BadRequestException(WRONG_ACTION_STATUS);

        cancelAction(action);
    }

    private void cancelAction(Action action) {
        if (action.getEnergyCost() == null) {
            if (!bonusActionService.addBonus(action.getActor(), action.getType())) {
                // TODO: log
            }
            return;
        }
        playerService.addPlayerEnergy(action.getActor().getId(), action.getEnergyCost());
        actionRepository.delete(action);
    }

    private Player getPlayerAndValidate(Long gameId, Long userId) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);
        Game game = player.getGame();

        if (player.getStatus() != PlayerStatus.PLANNING) {
            throw new BadRequestException(WRONG_PLAYER_STATUS);
        }

        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new BadRequestException(WRONG_GAME_STATE);
        }

        return player;
    }

    private Action findActionById(Long actionId) {
        return actionRepository.findById(actionId).orElseThrow(() ->
                new ResourceNotFoundException(ACTION, ID, actionId));
    }

    @Transactional
    public void endTurn(Long gameId, Long userId) {
        Player player = getPlayerAndValidate(gameId, userId);

        boolean playerHasBonuses = player.getBonuses().stream()
                .filter(bonus -> bonus.getType() != ActionType.ATTACK)
                .anyMatch(bonus -> bonus.getQuantity() > 0);

        if (playerHasBonuses) {
            throw new BadRequestException(PLAYER_HAS_BONUSES);
        }

        player.setStatus(PlayerStatus.COMMITED_ACTIONS);

        if (player.getGame().isForceNextTurn()) eventPublisher.publishEvent(new PlayerCommitedActionsEvent(gameId));
    }
}
