package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.ActionMapper;
import com.example.aquaticboogaloo.dto.request.ActionRequest;
import com.example.aquaticboogaloo.dto.response.ActionCreationResponse;
import com.example.aquaticboogaloo.dto.response.FailedValidationActionResponse;
import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import com.example.aquaticboogaloo.repository.ActionRepository;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.exception.BadRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class ActionValidationService {
    public static final String PARAMETER_IS_NULL = "Parameter '%s' is null";
    public static final String LOCATION_IS_OUT_OF_BOUNDS = "Location is out of game field bounds";
    public static final String UNKNOWN_ACTION_TYPE = "Action type '%s' not supported";
    public static final String PLACEMENT_ON_OWN_SHIPS = "Placement on own ships is restricted";
    public static final String PLACEMENT_NOT_IN_VISION_AREA = "Not in player's vision area";
    public static final String PLACEMENT_IN_VISION_AREA = "Placement in player's vision area is restricted";
    public static final String ACTION_ALREADY_EXISTS = "Action is already exists";

    private final PlayerService playerService;
    private final ActionRepository actionRepository;
    private final ActionMapper actionMapper;
    private final BonusActionService bonusActionService;

    /** Validates and then creates Player-Actions from ActionRequest list
     *  Subtracts Player energy (or Bonus) but player still have opportunity to cancel any of them actions (until player ends turn)

        Returns list of successfully added Actions
        and list of Actions that failed validation with cause message
     */
    @Transactional
    public ActionCreationResponse createActions(Player player, List<ActionRequest> actionRequests) {
        Game game = player.getGame();

        var response = new ActionCreationResponse();
        Set<Action> actions = new TreeSet<>(
                Comparator.comparingInt(Action::getCreatedAtTurn)
                        .thenComparing(Action::getType)
                        .thenComparingInt(Action::getLocationX)
                        .thenComparingInt(Action::getLocationY)
        );
        actions.addAll(player.getActions());

        int actionsEnergySum = 0;
        for (ActionRequest actionRequest : actionRequests) {
            var failedValidation = validateActionRequest(actionRequest, player);
            if (failedValidation != null) {
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

            // try to add Action to set
            // if fail - report duplication
            if (!actions.add(action)) {
                var dupAction = new FailedValidationActionResponse(actionRequest);
                dupAction.addCause(ACTION_ALREADY_EXISTS);
                response.add(dupAction);
                continue;
            }

            // set energy cost/use bonus
            action.setEnergyCost(
                    useBonusAction(player, actionRequest.type())
                            ? null
                            : game.getRuleset().getEnergyCost(actionRequest.type())
            );

            // save Action
            // might be duplications in concurrency
            // TODO: create unique index on action
            //  or unique constraint
            action = actionRepository.save(action);
            actionsEnergySum += action.getEnergyCost() == null ? 0 : action.getEnergyCost();
            response.add(actionMapper.toResponse(action));
        }

        playerService.subtractPlayerEnergy(player.getId(), actionsEnergySum);

        return response;
    }

    /**
     * @param player
     * @param actionType
     * @return true if bonus have been used
     */
    private boolean useBonusAction(Player player, ActionType actionType) {
        return bonusActionService.removeBonus(player, actionType);
    }

    private FailedValidationActionResponse validateActionRequest(ActionRequest actionRequest, Player player) {
        var response = new FailedValidationActionResponse(actionRequest);

        if (actionRequest.type() == null) response.addCause(PARAMETER_IS_NULL.formatted("TYPE"));
        if (actionRequest.locationY() == null) response.addCause(PARAMETER_IS_NULL.formatted("LOCATION_X"));
        if (actionRequest.locationX() == null) response.addCause(PARAMETER_IS_NULL.formatted("LOCATION_Y"));
        if (!response.isEmpty()) return response;

        // TODO: might be problem with included/excluded bounds
        if (!isIntInRange(actionRequest.locationX(), 0, player.getGame().getFieldWidth())
                || !isIntInRange(actionRequest.locationY(), 0, player.getGame().getFieldHeight())) {
            response.addCause(LOCATION_IS_OUT_OF_BOUNDS);
            return response;
        }

        response.addAllCauses(
                switch (actionRequest.type()) {
                    case ATTACK -> attackPlacementValidation(actionRequest.locationX(), actionRequest.locationY(), player);
                    case SCAN -> scanPlacementValidation(actionRequest.locationX(), actionRequest.locationY(), player);
                    case PLACE_SHIELD -> shieldPlacementValidation(actionRequest.locationX(), actionRequest.locationY(), player);
                    case PLACE_MINE -> minePlacementValidation(actionRequest.locationX(), actionRequest.locationY(), player);
                    // TODO: log
                    default -> List.of(UNKNOWN_ACTION_TYPE.formatted(actionRequest.type()));
                }
        );


        return response.isEmpty() ? null : response;
    }

    private List<String> minePlacementValidation(int x, int y, Player player) {
        /*  can be placed only in visible area (around own ships)
        *   TODO: cell must be empty (no mines/shields)
         */
        List<String> list = new ArrayList<>();

        if (!isVisionArea(x, y, player)) list.add(PLACEMENT_NOT_IN_VISION_AREA);
        if (isOwnShipCell(x, y, player)) list.add(PLACEMENT_ON_OWN_SHIPS);

        return list;
    }
    private List<String> attackPlacementValidation(int x, int y, Player player) {
        /*  is non-visible area
         */
        List<String> list = new ArrayList<>();

        if (isVisionArea(x, y, player)) list.add(PLACEMENT_IN_VISION_AREA);

        return list;
    }
    private List<String> scanPlacementValidation(int x, int y, Player player) {
        /*  is non-visible area (correction: non-ownShipCell)
         */
        List<String> list = new ArrayList<>();

        if (isOwnShipCell(x, y, player)) list.add(PLACEMENT_ON_OWN_SHIPS);

        return list;
    }
    private List<String> shieldPlacementValidation(int x, int y, Player player) {
        /*  no restrictions
        */
        List<String> list = new ArrayList<>();

        return list;
    }

    private boolean isVisionArea(int x, int y, Player player) {
        final int vRad = player.getGame().getRuleset().getVisionRadius();
        return player.getShips().stream()
                .filter(ship -> ship.getStatus() != ShipStatus.DESTROYED)
                .anyMatch(ship -> {
                ship.getShipCells().sort(ShipCell::compareTo);
                var scMin = ship.getShipCells().getFirst();
                var scMax = ship.getShipCells().getLast();
                return (isIntInRange(x, scMin.getLocationX()-vRad, scMax.getLocationX()+vRad)
                        && isIntInRange(y, scMin.getLocationY()-vRad, scMax.getLocationY()+vRad));
        });
    }

    private boolean isOwnShipCell(int x, int y, Player player) {
        return player.getShips().stream()
                .filter(ship -> ship.getStatus() != ShipStatus.DESTROYED)
                .flatMap(ship -> ship.getShipCells().stream())
                .anyMatch(sc -> sc.getLocationX() == x && sc.getLocationY() == y);
    }

    private boolean isIntInRange(int i, int min, int max) {
        return i >= min && i <= max;
    }

}
