package com.example.aquaticboogaloo.service;


import com.example.aquaticboogaloo.dto.request.ActionRequest;
import com.example.aquaticboogaloo.dto.response.action.FailedValidationActionResponse;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import com.example.aquaticboogaloo.repository.MineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionValidator {

    public static final String ACTION_REQUEST_IS_NULL = "ActionRequest is null";
    public static final String PARAMETER_IS_NULL = "Parameter '%s' is null";
    public static final String LOCATION_IS_OUT_OF_BOUNDS = "Location is out of game field bounds";
    public static final String UNKNOWN_ACTION_TYPE = "Action type '%s' not supported";
    public static final String PLACEMENT_ON_OWN_SHIPS = "Placement on own ships is restricted";
    public static final String PLACEMENT_ON_MINE = "Placement blocked by mine";
    public static final String PLACEMENT_NOT_IN_VISION_AREA = "Not in player's vision area";
    public static final String PLACEMENT_IN_VISION_AREA = "Placement in player's vision area is restricted";
    public static final String ACTION_ALREADY_EXISTS = "Action is already exists";
    public static final String INSUFFICIENT_ENERGY = "Insufficient energy";

    private final MineRepository mineRepository;


    public List<String> validateActionRequest(
            ActionRequest actionRequest,
            Player player
    ) {
        List<String> errors = new LinkedList<>();

        if (actionRequest == null) {
            errors.add(ACTION_REQUEST_IS_NULL);
            return errors;
        }
        if (actionRequest.type() == null) errors.add(PARAMETER_IS_NULL.formatted("TYPE"));
        if (actionRequest.locationY() == null) errors.add(PARAMETER_IS_NULL.formatted("LOCATION_Y"));
        if (actionRequest.locationX() == null) errors.add(PARAMETER_IS_NULL.formatted("LOCATION_X"));
        if (!errors.isEmpty()) return errors;

        if (!isIntInRange(actionRequest.locationX(), 0, player.getGame().getFieldWidth() - 1)
                || !isIntInRange(actionRequest.locationY(), 0, player.getGame().getFieldHeight() - 1)) {
            errors.add(LOCATION_IS_OUT_OF_BOUNDS);
            return errors;
        }

        errors.addAll(actionPlacementValidation(actionRequest, player));

        return errors;
    }

    public List<String> actionPlacementValidation(ActionRequest actionRequest, Player player) {
        return switch (actionRequest.type()) {
            case ATTACK -> attackPlacementValidation(
                    actionRequest.locationX(),
                    actionRequest.locationY(),
                    player
            );
            case PLACE_SCAN -> scanPlacementValidation(
                    actionRequest.locationX(),
                    actionRequest.locationY(),
                    player
            );
            case PLACE_SHIELD -> shieldPlacementValidation(
                    actionRequest.locationX(),
                    actionRequest.locationY(),
                    player
            );
            case PLACE_MINE -> minePlacementValidation(
                    actionRequest.locationX(),
                    actionRequest.locationY(),
                    player
            );
            // TODO: log
            default -> List.of(UNKNOWN_ACTION_TYPE.formatted(actionRequest.type()));
        };
    }

    private List<String> minePlacementValidation(int x, int y, Player player) {
        /*  can be placed only in visible area (around own ships)
         */
        List<String> list = new ArrayList<>();

        if (!isVisionArea(x, y, player)) list.add(PLACEMENT_NOT_IN_VISION_AREA);
        else if (isOwnShipCell(x, y, player)) list.add(PLACEMENT_ON_OWN_SHIPS);
        else if (isMine(x, y, player)) list.add(PLACEMENT_ON_MINE);

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
                    return (isIntInRange(x, scMin.getLocationX() - vRad, scMax.getLocationX() + vRad)
                            && isIntInRange(y, scMin.getLocationY() - vRad, scMax.getLocationY() + vRad));
                });
    }

    private boolean isOwnShipCell(int x, int y, Player player) {
        return player.getShips().stream()
                .filter(ship -> ship.getStatus() != ShipStatus.DESTROYED)
                .flatMap(ship -> ship.getShipCells().stream())
                .anyMatch(sc -> sc.getLocationX() == x && sc.getLocationY() == y);
    }

    private boolean isMine(int x, int y, Player player) {
        return mineRepository.existsByCoordinate(player.getGame().getId(), x, y);
    }

    private boolean isIntInRange(int i, int min, int max) {
        return i >= min && i <= max;
    }
}
