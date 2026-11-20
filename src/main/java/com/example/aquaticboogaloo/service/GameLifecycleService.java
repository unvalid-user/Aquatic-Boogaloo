package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.request.CreateGameRequest;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.*;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.repository.GameRepository;
import com.example.aquaticboogaloo.service.field_initializer.GameFieldInitializer;
import com.example.aquaticboogaloo.service.resolve_turn.ResolveActionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.NOT_ENOUGH_PLAYERS;
import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_GAME_STATE;

@Service
@RequiredArgsConstructor
public class GameLifecycleService {

    private final GameService gameService;
    private final UserService userService;
    private final ResolveActionService resolveActionService;
    private final PlayerService playerService;
    private final ScanService scanService;
    private final ShieldService shieldService;

    private final GameRepository gameRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Game createGame(CreateGameRequest request, Long userId) {
        User user = userService.findUserById(userId);

        Game game = Game.buildWithRuleset(user);
        game.setTitle(request.getTitle());
        game.setDescription(request.getDescription());
        game.setRemainTurns(request.getTurns());
        game.setTurnDuration(Duration.ofSeconds(request.getTurnSeconds()));
        game.setRequestToJoin(request.isRequestToJoin());
        game.setForceNextTurn(request.isForceNextTurn());

        if (request.getPassword() != null && !request.getPassword().isBlank())
            game.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));

        return gameRepository.save(game);
    }

    @Transactional
    public void deleteGame(Long gameId, Long userId) {
        Game game = gameService.findForUpdate(gameId);

        if (!game.getHostUser().getId().equals(userId))
            throw new AccessDeniedException();

        if (game.getStatus() != GameStatus.NEW)
            throw new BadRequestException(WRONG_GAME_STATE);

        gameRepository.delete(game);
    }

    // TODO: test
    @Transactional
    public Game startGame(Long gameId, Long hostId) {
        Game game = gameService.findForUpdate(gameId);

        if (!game.getHostUser().getId().equals(hostId))
            throw new AccessDeniedException();
        if (game.getStatus() != GameStatus.NEW)
            throw new BadRequestException(WRONG_GAME_STATE);

        if (game.getPlayers().size() < 2)
            throw new BadRequestException(NOT_ENOUGH_PLAYERS);

        game.setStatus(GameStatus.RESOLVING);
        game.setCurrentTurn(1);

        GameFieldInitializer initializer = new GameFieldInitializer(game);
        initializer.initializeAndBuildEntities();

        game.getPlayers().forEach(player -> {
            setDefaultValuesForPlayer(player);
            accrueShipBonuses(player);
        });

        game.setTurnAdvanceAt();
        game.setStatus(GameStatus.ACTIVE);
        return game;

        /* 1. initialize game field and Ships' cords
         * 2. add Bonuses for Ships to Players
         * 3. update Players
         * 4. set nextTurn time
         * 5. Change Game status
         *
         *  TODO: Host can preview field and re-generate it
         */
    }

    @Transactional
    public void resolveTurnIfAllPlayersReady(Long gameId) {
        Game game = gameService.findForUpdate(gameId);

        if (game.getStatus() != GameStatus.ACTIVE || !game.isForceNextTurn()) return;
        if (playerService.countPlanningPlayersByGameId(gameId) > 0) return;
        resolveLockedTurn(game);
    }

    @Transactional
    public void resolveTurnIfExpired(Long gameId) {
        Game game = gameService.findForUpdate(gameId);

        if (game.getStatus() != GameStatus.ACTIVE
                || game.getTurnAdvanceAt() == null
                || game.getTurnAdvanceAt().isAfter(Instant.now()))
            return;
        resolveLockedTurn(game);
    }

    /*
     1. atomic update game status
     2. resolve planned actions
     3. calculate ships new statuses
     4. calculate players new statuses
     5. reset players energy
     6. give bonuses to players: turnSurvived + ship bonuses
     7. delete expired scans and shields
     8. increase gameTurn + update game status

     TODO: add game resolve log
     */
    @Transactional
    public void resolveTurn(Long gameId) {
        Game game = gameService.findForUpdate(gameId);

        if (game.getStatus() != GameStatus.ACTIVE) return;
        resolveLockedTurn(game);
    }

    private void resolveLockedTurn(Game game) {
        game.setStatus(GameStatus.RESOLVING);

        // resolving planned actions
        resolvePlannedActions(game);

        // updating ships' and players' statuses
        updateShipStatuses(game);
        updatePlayerStatuses(game);

        var alivePlayers = game.getPlayers().stream()
                .filter(player -> player.getStatus() != PlayerStatus.DEAD)
                .toList();

        // TODO: might be a problem with non-relevant data (player.actions)
        // resetting players' energy + adding bonuses for ships + bonuses for surviving
        alivePlayers.forEach(player -> {
            player.getBonuses().clear();
            player.setEnergy(game.getRuleset().getDefaultPlayerEnergy());
            player.addPoints(game.getRuleset().getTurnSurviveBonus());

            player.setStatus(PlayerStatus.PLANNING);

            accrueShipBonuses(player);
        });
        // adding bonus if player didn't plan any actions (except bonuses)
        alivePlayers.stream()
                .filter(player -> player.getActions().stream()
                        .filter(a -> a.getCreatedAtTurn() == game.getCurrentTurn())
                        .allMatch(Action::isBonus))
                .forEach(player -> {
                    accrueBonus(
                            player,
                            game.getRuleset().getSkipTurnBonusType(),
                            game.getRuleset().getSkipTurnBonusQuantity()
                    );
                });

        // increase game turn
        game.advanceTurn();

        // deleting expired field objects
        scanService.deleteExpiredScans(game);
        shieldService.deleteExpiredShields(game);

        if (
                (game.getRemainTurns() != null && game.getRemainTurns() <= 0)
                || alivePlayers.isEmpty()
        ) {
            game.setStatus(GameStatus.ENDED);
            game.setEndsAt(Instant.now());
            game.setTurnAdvanceAt(null);
        } else {
            game.setStatus(GameStatus.ACTIVE);
        }
    }

    private int updateShipStatuses(Game game) {
        var updatedShips = game.getPlayers().stream()
                .flatMap(player -> player.getShips().stream())
                .filter(ship ->
                        ship.getStatus() != ShipStatus.DESTROYED &&
                        ship.getShipCells().stream().allMatch(ShipCell::isDestroyed)
                )
                .toList();

        updatedShips.forEach(ship -> ship.setStatus(ShipStatus.DESTROYED));

        return updatedShips.size();
    }

    private int updatePlayerStatuses(Game game) {
        var updatedPlayers = game.getPlayers().stream()
                .filter(player ->
                        player.getStatus() != PlayerStatus.DEAD &&
                        player.getShips().stream()
                                .allMatch(ship -> ship.getStatus() == ShipStatus.DESTROYED)
                )
                .toList();

        updatedPlayers.forEach(player -> player.setStatus(PlayerStatus.DEAD));

        return updatedPlayers.size();
    }

    private void resolvePlannedActions(Game game) {
        // TODO: transactions and exceptions
        resolveActionService.resolveShieldActions(game);
        resolveActionService.resolveMineActions(game);
        resolveActionService.resolveAttackActions(game);
        resolveActionService.resolveScanActions(game);
    }

    private void setDefaultValuesForPlayer(Player player) {
        GameRuleset ruleset = player.getGame().getRuleset();

        player.setStatus(PlayerStatus.PLANNING);
        player.setEnergy(ruleset.getDefaultPlayerEnergy());
        player.setPoints(ruleset.getDefaultPlayerPoints());
    }

    private void accrueShipBonuses(Player player) {
        GameRuleset ruleset = player.getGame().getRuleset();

        player.getShips().stream()
                .filter(ship -> ship.getStatus() != ShipStatus.DESTROYED)
                .collect(Collectors.groupingBy(Ship::getType))
                .forEach((shipType, ships) -> {
                    int n = ships.size();
                    if (n < 1) return;

                    ShipRule shipRule = ruleset.getShipRule(shipType);

                    accrueBonus(player, shipRule.bonusType(), shipRule.bonusQuantity() * n);
                });
    }

    public void accrueBonus(Player player, BonusType type, int quantity) {
        ActionType actionType = null;
        switch (type) {
            // TODO: race conditions possible
            case POINTS -> player.addPoints(quantity);
            case ENERGY -> player.setEnergy(player.getEnergy() + quantity);
            case FREE_ATTACK -> actionType = ActionType.ATTACK;
            case FREE_SHIELD -> actionType = ActionType.PLACE_SHIELD;
            case FREE_MINE -> actionType = ActionType.PLACE_MINE;
            case FREE_SCAN -> actionType = ActionType.PLACE_SCAN;
        }
        if (actionType == null) return;

        BonusAction bonus = new BonusAction();
        bonus.setType(actionType);
        bonus.setQuantity(quantity);

        player.addBonus(bonus);
    }
}
