package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.mapper.GameMapper;
import com.example.aquaticboogaloo.dto.mapper.field_object.FieldObjectMapper;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.GameRulesetResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.field_objects.Mine;
import com.example.aquaticboogaloo.entity.field_objects.Scan;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.repository.GameRepository;
import com.example.aquaticboogaloo.repository.specification.GameSpecifications;
import com.example.aquaticboogaloo.util.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GameResponseService {

    // TODO: too many dependencies
    private final GameRepository gameRepository;
    private final PlayerService playerService;
    private final ShipService shipService;
    private final MineService mineService;
    private final ScanService scanService;
    private final GameService gameService;
    private final AttackService attackService;

    private final FieldObjectMapper fieldObjectMapper;
    private final GameMapper gameMapper;

    public GameResponse buildGameResponseWithPlayersCount(Long gameId, Long userId) {
        return buildGameResponse(
                gameService.findGameById(gameId),
                gameService.getPlayersCountByGameId(gameId),
                userId
        );
    }

    public PagedResponse<GameResponse> findJoinedGamesPaged(
            Pageable pageable,
            GameFilter filter,
            Long userId
    ) {
        var specs = GameSpecifications.withFilterAndJoinedByUser(filter, userId);
        return findAllPaged(pageable, specs, userId);
    }

    public PagedResponse<GameResponse> findModeratedGamesPaged(
            Pageable pageable,
            GameFilter gameFilter,
            Long userId
    ) {
        var specs = GameSpecifications.withFilterAndModeratedByUser(gameFilter, userId);
        return findAllPaged(pageable, specs, userId);
    }

    public PagedResponse<GameResponse> findAllPaged(
            Pageable pageable,
            GameFilter gameFilter,
            Long userId
    ) {
        var specs = GameSpecifications.withFilter(gameFilter);
        return findAllPaged(pageable, specs, userId);
    }

    private PagedResponse<GameResponse> findAllPaged(
            Pageable pageable,
            Specification<Game> specs,
            Long userId
    ) {
        Page<Game> gamesPage = gameRepository.findAll(specs, pageable);
        var gamePlayersCounts = gameService.getPlayersCountsByGameIds(
                gamesPage.map(Game::getId).getContent()
        );

        var gameResponsePage = gamesPage.map(game ->
                buildGameResponse(game, gamePlayersCounts.getOrDefault(game.getId(), 0), userId)
        );

        return PagedResponse.from(gameResponsePage);
    }

    public GameFieldResponse buildGameFieldResponseForModeratorView(Long gameId, Long userId) {
        Game game = gameService.findGameByIdAndHostIdOrModeratorId(gameId, userId);

        List<Ship> ships = shipService.findGameShips(gameId);
        List<Mine> mines = mineService.findGameMines(gameId);
        List<Scan> scans = scanService.findGameScans(gameId);

        return buildGameFieldResponse(game, ships, mines, scans);
    }

    public GameFieldResponse buildGameFieldResponseForPlayerView(Long gameId, Long userId) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);
//        if (player.getStatus() == PlayerStatus.DEAD) {
//            throw new AccessDeniedException();
//        }
        Game game = player.getGame();
        final int visionRadius = game.getRuleset().getVisionRadius();

        List<Ship> ships = player.getShips();
        Set<Mine> mines = new HashSet<>(mineService.findPlayerMines(player.getId()));
        List<Scan> scans = scanService.findPlayerScans(player.getId());
        ships.stream()
                .filter(ship -> ship.getStatus() != ShipStatus.DESTROYED)
                .forEach(ship -> {
                    Point min = ship.getShipCells().stream()
                            .map(sc -> new Point(sc.getLocationX(), sc.getLocationY()))
                            .min(Point::compareTo)
                            // TODO: exception 5XX
                            .orElseThrow();

                    Point max = ship.getShipCells().stream()
                            .map(sc -> new Point(sc.getLocationX(), sc.getLocationY()))
                            .max(Point::compareTo)
                            // TODO: exception 5XX
                            .orElseThrow();

                    mines.addAll(mineService.findMinesAroundShip(gameId, visionRadius, min, max));
                });

        var response = buildGameFieldResponse(game, ships, mines, scans);
        // other players attack in current player vision radius
        response.setAttacks(attackService.getEnemiesAttacksInVisionRadius(player));
        return response;
    }

    private GameResponse buildGameResponse(Game game, int playersCount, Long currentUserId) {
        GameResponse gameResponse = gameMapper.toResponse(game);
        gameResponse.setPlayersCount(playersCount);

        gameResponse.setJoinedByCurrentUser(
                // TODO: fix N+1
                currentUserId != null && playerService.existsByUserAndGame(game.getId(), currentUserId)
        );

        return gameResponse;
    }

    private GameFieldResponse buildGameFieldResponse(
            Game game,
            Collection<Ship> ships,
            Collection<Mine> mines,
            Collection<Scan> scans
    ) {
        var response = new GameFieldResponse();
        response.setFieldHeight(game.getFieldHeight());
        response.setFieldWidth(game.getFieldWidth());
        response.setShips(ships.stream().map(fieldObjectMapper::toResponse).toList());
        response.setMines(mines.stream().map(fieldObjectMapper::toResponse).toList());
        response.setScans(scans.stream().map(fieldObjectMapper::toResponse).toList());

        return response;
    }

    public GameRulesetResponse getGameRuleset(Long gameId) {
        Game game = gameService.findGameById(gameId);

        return gameMapper.toResponse(game.getRuleset());
    }
}
