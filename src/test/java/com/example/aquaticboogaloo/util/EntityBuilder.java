package com.example.aquaticboogaloo.util;

import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.entity.enums.ShipType;
import com.example.aquaticboogaloo.entity.field_objects.*;

public class EntityBuilder {
    public static Player player(User user) {
        Player player = new Player();
        player.setUser(user);
        player.setStatus(PlayerStatus.PLANNING);
        player.setEnergy(10);
        player.setPoints(0);
        return player;
    }

    public static Action action(ActionType type, int x, int y, int turn) {
        Action action = new Action();
        action.setType(type);
        action.setStatus(ActionStatus.PLANNED);
        action.setCreatedAtTurn(turn);
        action.setLocationX(x);
        action.setLocationY(y);
        action.setEnergyCost(1);
        return action;
    }

    public static Mine mine(int x, int y, int turn) {
        Action action = action(ActionType.PLACE_MINE, x, y, turn);
        action.setStatus(ActionStatus.COMPLETED);

        Mine mine = new Mine();
        mine.setAction(action);

        return mine;
    }

    public static Shield shield(int x, int y, int turn) {
        Action action = action(ActionType.PLACE_SHIELD, x, y, turn);
        action.setStatus(ActionStatus.COMPLETED);

        Shield shield = new Shield();
        shield.setAction(action);

        return shield;
    }

    public static Ship shipWithOneCell(int x, int y) {
        Ship ship = new Ship();
        ship.setType(ShipType.K1);

        ShipCell cell = new ShipCell();
        cell.setLocationX(x);
        cell.setLocationY(y);
        ship.addCell(cell);

        return ship;
    }

    public static Ship shipWithTwoCells(int x, int y) {
        Ship ship = new Ship();
        ship.setType(ShipType.K2);

        ShipCell cell1 = new ShipCell();
        cell1.setLocationX(x);
        cell1.setLocationY(y);
        ship.addCell(cell1);

        ShipCell cell2 = new ShipCell();
        cell2.setLocationX(x);
        cell2.setLocationY(y-1);
        ship.addCell(cell2);

        return ship;
    }
}
