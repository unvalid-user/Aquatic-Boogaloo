package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.ActionMapper;
import com.example.aquaticboogaloo.dto.request.ActionRequest;
import com.example.aquaticboogaloo.dto.response.action.ActionCreationResponse;
import com.example.aquaticboogaloo.dto.response.action.FailedValidationActionResponse;
import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.repository.ActionRepository;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.util.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionCreationService {

    private final PlayerService playerService;
    private final BonusActionService bonusActionService;
    private final ActionValidator actionValidator;
    private final ActionRepository actionRepository;

    private final ActionMapper actionMapper;

    /** Validates and then creates Player-Actions from ActionRequest list
     *  Subtracts Player energy (or Bonus) but player still have opportunity to cancel any of them actions (until player ends turn)

        Returns list of successfully added Actions
        and list of Actions that failed validation with cause message
     */
    @Transactional
    public ActionCreationResponse createActions(Player player, List<ActionRequest> actionRequests) {
        Game game = player.getGame();

        var response = new ActionCreationResponse();
        Set<Point> occupiedCells = player.getActions().stream()
                .filter(a -> a.getCreatedAtTurn() == game.getCurrentTurn())
                .map(a -> new Point(a.getLocationX(), a.getLocationY()))
                .collect(Collectors.toSet());

        int energyCostSum = 0;
        for (ActionRequest actionRequest : actionRequests) {
            var errors = actionValidator.validateActionRequest(actionRequest, player);
            if (!errors.isEmpty()) {
                var failedValidation = new FailedValidationActionResponse(actionRequest);
                response.add(failedValidation);
                continue;
            }

            //  if ActionRequest passes all validations, create Action
            Action action = new Action();
            action.setActor(player);
            action.setCreatedAtTurn(game.getCurrentTurn());
            action.setType(actionRequest.type());
            action.setLocationX(actionRequest.locationX());
            action.setLocationY(actionRequest.locationY());

            Point actionCell = new Point(actionRequest.locationX(), actionRequest.locationY());

            // if cell occupied - report duplication
            if (occupiedCells.contains(actionCell)) {
                response.addFailure(actionRequest, ActionValidator.ACTION_ALREADY_EXISTS);
                continue;
            }

            // trying to use bonus
            if (!useBonusAction(player, actionRequest.type())) {
                int energyCost = game.getRuleset().getEnergyCost(actionRequest.type());

                // check if player has enough energy
                if (energyCostSum + energyCost > player.getEnergy()) {
                    response.addFailure(actionRequest, ActionValidator.INSUFFICIENT_ENERGY);
                    continue;
                }

                action.setEnergyCost(energyCost);
                energyCostSum += energyCost;
            }

            occupiedCells.add(actionCell);
            // The game lock and database constraint protect against concurrent duplicates.
            action = actionRepository.save(action);
            player.addAction(action);
            response.add(actionMapper.toResponse(action));
        }

        playerService.subtractPlayerEnergy(player.getId(), energyCostSum);

        return response;
    }

    /**
     * true if bonus has been used
     */
    private boolean useBonusAction(Player player, ActionType actionType) {
        return bonusActionService.removeBonus(player, actionType);
    }
}
