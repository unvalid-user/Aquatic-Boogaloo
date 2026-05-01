package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.ActionMapper;
import com.example.aquaticboogaloo.dto.request.ActionRequest;
import com.example.aquaticboogaloo.dto.response.action.ActionResponse;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.entity.enums.ShipType;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import com.example.aquaticboogaloo.repository.ActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActionValidationServiceTest {
    @Mock
    private PlayerService playerService;
    @Mock
    private BonusActionService bonusActionService;
    @Mock
    private ActionRepository actionRepository;
    @Mock
    private ActionMapper actionMapper;

    @InjectMocks
    private ActionValidationService actionValidationService;


    @Test
    void createActions_shouldCreateAll() {
        Player player = buildTestPlayer();
        Long gameId = player.getUser().getId();
        Long userId = player.getGame().getId();

        List<ActionRequest> actionRequestList = new ArrayList<>();
        actionRequestList.add(new ActionRequest(9, 9, ActionType.ATTACK));
        actionRequestList.add(new ActionRequest(2, 2, ActionType.PLACE_SHIELD));
        actionRequestList.add(new ActionRequest(1, 1, ActionType.PLACE_MINE));
        actionRequestList.add(new ActionRequest(4, 4, ActionType.SCAN));

        doAnswer(inv -> {
            int amount = inv.getArgument(1);
            player.setEnergy(player.getEnergy() - amount);
            return null;
        }).when(playerService).subtractPlayerEnergy(eq(player.getId()), anyInt());
        when(actionRepository.save(any(Action.class))).thenAnswer(inv -> {
            Action action = inv.getArgument(0);
            action.setId(999L);

            return action;
        });
        when(bonusActionService.removeBonus(any(Player.class), any(ActionType.class))).thenAnswer(inv -> {
            Player player1 = inv.getArgument(0);
            ActionType actionType = inv.getArgument(1);
            BonusAction bonusAction = player1.getBonuses().stream()
                    .filter(bonus -> bonus.getType() == actionType)
                    .findFirst()
                    .orElse(null);

            if (bonusAction == null || bonusAction.getQuantity() < 1) return false;

            bonusAction.setQuantity(bonusAction.getQuantity() - 1);
            return true;
        });
        when(actionMapper.toResponse(any(Action.class))).thenAnswer(inv -> {
            Action action = inv.getArgument(0);
            ActionResponse response = new ActionResponse();
            response.setId(action.getId());
            response.setPlayerId(action.getActor().getId());
            response.setEnergyCost(action.getEnergyCost());
            response.setCreatedAtTurn(action.getCreatedAtTurn());
            response.setLocationX(action.getLocationX());
            response.setLocationY(action.getLocationY());
            response.setType(action.getType());
            response.setStatus(action.getStatus());
            response.setFailCauseMessage(action.getFailCauseMessage());

            return response;
        });

        var response = actionValidationService.createActions(player, actionRequestList);

        assertThat(response.getCreatedActions().size()).isEqualTo(actionRequestList.size());
    }



    @Test
    void createActions_shouldFailAll() {
        Player player = buildTestPlayer();
        Long gameId = player.getUser().getId();
        Long userId = player.getGame().getId();

        List<ActionRequest> actionRequestList = new ArrayList<>();
        actionRequestList.add(new ActionRequest(2, 2, ActionType.ATTACK));
        actionRequestList.add(new ActionRequest(11, -1, ActionType.PLACE_SHIELD));
        actionRequestList.add(new ActionRequest(2, 3, ActionType.PLACE_MINE));
        actionRequestList.add(new ActionRequest(10, 10, ActionType.PLACE_MINE));
        actionRequestList.add(new ActionRequest(2, 4, ActionType.SCAN));
        actionRequestList.add(new ActionRequest(2, 4, null));
        actionRequestList.add(new ActionRequest(null, null, ActionType.SCAN));

        doAnswer(inv -> {
            int amount = inv.getArgument(1);
            player.setEnergy(player.getEnergy() - amount);
            return null;
        }).when(playerService).subtractPlayerEnergy(eq(player.getId()), anyInt());

        var response = actionValidationService.createActions(player, actionRequestList);

        assertThat(response.getFailedActionRequests().size()).isEqualTo(actionRequestList.size());
    }


    @Test
    void createActions_shouldFailDuplicatedActions() {
        Player player = buildTestPlayer();
        Long gameId = player.getUser().getId();
        Long userId = player.getGame().getId();

        Action action1 = new Action();
        action1.setId(2L);
        action1.setEnergyCost(2);
        action1.setCreatedAtTurn(player.getGame().getCurrentTurn());
        action1.setLocationX(1);
        action1.setLocationY(1);
        action1.setType(ActionType.PLACE_MINE);

        Action action2 = new Action();
        action2.setId(1L);
        action2.setEnergyCost(2);
        action2.setCreatedAtTurn(0);
        action2.setLocationX(2);
        action2.setLocationY(2);
        action2.setType(ActionType.PLACE_SHIELD);

        player.addAction(action1);
        player.addAction(action2);


        List<ActionRequest> actionRequestList = new ArrayList<>();
        actionRequestList.add(new ActionRequest(9, 9, ActionType.ATTACK));
        actionRequestList.add(new ActionRequest(9, 9, ActionType.ATTACK));
        actionRequestList.add(new ActionRequest(2, 2, ActionType.PLACE_SHIELD));
        actionRequestList.add(new ActionRequest(1, 1, ActionType.PLACE_MINE));
        actionRequestList.add(new ActionRequest(4, 4, ActionType.SCAN));

        doAnswer(inv -> {
            int amount = inv.getArgument(1);
            player.setEnergy(player.getEnergy() - amount);
            return null;
        }).when(playerService).subtractPlayerEnergy(eq(player.getId()), anyInt());
        when(actionRepository.save(any(Action.class))).thenAnswer(inv -> {
            Action action = inv.getArgument(0);
            action.setId(999L);

            return action;
        });
        when(bonusActionService.removeBonus(any(Player.class), any(ActionType.class))).thenAnswer(inv -> {
            Player player1 = inv.getArgument(0);
            ActionType actionType = inv.getArgument(1);
            BonusAction bonusAction = player1.getBonuses().stream()
                    .filter(bonus -> bonus.getType() == actionType)
                    .findFirst()
                    .orElse(null);

            if (bonusAction == null || bonusAction.getQuantity() < 1) return false;

            bonusAction.setQuantity(bonusAction.getQuantity() - 1);
            return true;
        });
        when(actionMapper.toResponse(any(Action.class))).thenAnswer(inv -> {
            Action action = inv.getArgument(0);
            ActionResponse response = new ActionResponse();
            response.setId(action.getId());
            response.setPlayerId(action.getActor().getId());
            response.setEnergyCost(action.getEnergyCost());
            response.setCreatedAtTurn(action.getCreatedAtTurn());
            response.setLocationX(action.getLocationX());
            response.setLocationY(action.getLocationY());
            response.setType(action.getType());
            response.setStatus(action.getStatus());
            response.setFailCauseMessage(action.getFailCauseMessage());

            return response;
        });

        var response = actionValidationService.createActions(player, actionRequestList);

        assertThat(response.getCreatedActions().size()).isEqualTo(3);
        assertThat(response.getFailedActionRequests().size()).isEqualTo(2);
    }


    Player buildTestPlayer() {
        Ship ship = new Ship();
        ship.setId(1L);
        ship.setType(ShipType.K4);

        for (int i = 0; i < 4; i++) {
            ShipCell shipCell = new ShipCell();
            shipCell.setId((long)i);
            shipCell.setLocationX(2);
            shipCell.setLocationY(2+i);

            ship.addCell(shipCell);
        }

        BonusAction bonusAction = new BonusAction();
        bonusAction.setId(1L);
        bonusAction.setType(ActionType.SCAN);
        bonusAction.setQuantity(1);

        User user = new User();
        user.setId(3L);

        Player player = new Player();
        player.setId(1L);
        player.setUser(user);
        player.setEnergy(10);
        player.setPoints(0);
        player.setStatus(PlayerStatus.PLANNING);

        player.addShip(ship);
        player.addBonus(bonusAction);

        Game game = new Game();
        game.setId(5L);
        game.setStatus(GameStatus.ACTIVE);
        game.setFieldHeight(10);
        game.setFieldWidth(10);
        game.setCurrentTurn(1);
        game.setRuleset(new GameRuleset());

        game.addPlayer(player);

        return player;
    }
}
