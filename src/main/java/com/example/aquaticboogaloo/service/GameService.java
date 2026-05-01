package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.mapper.GameMapper;
import com.example.aquaticboogaloo.dto.mapper.field_object.FieldObjectMapper;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.field_objects.Mine;
import com.example.aquaticboogaloo.entity.field_objects.Scan;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.GameRepository;
import com.example.aquaticboogaloo.repository.projection.GamePlayersCountProjection;
import com.example.aquaticboogaloo.repository.specification.GameSpecifications;
import com.example.aquaticboogaloo.util.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_GAME_STATE;
import static com.example.aquaticboogaloo.util.EntityConst.GAME;
import static com.example.aquaticboogaloo.util.EntityConst.ID;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final PlayerService playerService;
    private final ShipService shipService;
    private final MineService mineService;
    private final ScanService scanService;
    private final FieldObjectMapper fieldObjectMapper;

    public void updateGameStatus(Long gameId, GameStatus currentStatus, GameStatus newStatus) {
        int rows = gameRepository.updateGameStatus(gameId, currentStatus, newStatus);

        if (rows < 1) throw new BadRequestException(WRONG_GAME_STATE);
    }

    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() ->
                new ResourceNotFoundException(GAME, ID, gameId));
    }

    public GameResponse buildGameResponseWithPlayersCount(Long gameId, Long userId) {
        return buildGameResponse(
                findGameById(gameId),
                getPlayersCountByGameId(gameId),
                userId
        );
    }

    public Game findGameByIdAndHostId(Long gameId, Long userId) {
        return gameRepository.findByIdAndHostUser_Id(gameId, userId)
                .orElseThrow(AccessDeniedException::new);
    }
    public Game findGameByIdAndHostIdOrModeratorId(Long gameId, Long userId) {
        return gameRepository.findGameByIdAndHostIdOrModeratorId(gameId, userId)
                .orElseThrow(AccessDeniedException::new);
    }

    public boolean isUserGameModerator(Long gameId, Long userId) {
        return gameRepository.findGameByIdAndHostIdOrModeratorId(gameId, userId)
                .isPresent();
    }

    public List<Long> getGameIdsWithExpiredTurn() {
        return gameRepository.findGameIdsWithExpiredTurn(Instant.now(), GameStatus.ACTIVE);
    }

    public int getPlayersCountByGameId(Long gameId) {
        return gameRepository.countPlayersByGameIds(List.of(gameId))
                .getFirst()
                .getPlayersCount();
    }

    public Map<Long, Integer> getPlayersCountsByGameIds(List<Long> gameIds) {
        return gameRepository.countPlayersByGameIds(gameIds)
                .stream()
                .collect(Collectors.toMap(
                        GamePlayersCountProjection::getGameId,
                        GamePlayersCountProjection::getPlayersCount
                ));
    }

    public PagedResponse<GameResponse> findModeratedGamesPaged(Pageable pageable, GameFilter gameFilter, Long userId) {
        var specs = GameSpecifications.withFilter(gameFilter, userId);
        return findAllPaged(pageable, specs, null);
    }
    public PagedResponse<GameResponse> findAllPaged(Pageable pageable, GameFilter gameFilter, Long userId) {
        var specs = GameSpecifications.withFilter(gameFilter);
        return findAllPaged(pageable, specs, userId);
    }
    private PagedResponse<GameResponse> findAllPaged(Pageable pageable, Specification<Game> specs, Long userId) {
        Page<Game> gamesPage = gameRepository.findAll(specs, pageable);
        var gamePlayersCounts = getPlayersCountsByGameIds(gamesPage.map(Game::getId).getContent());

        var gameResponsePage = gamesPage.map(game ->
            buildGameResponse(game, gamePlayersCounts.getOrDefault(game.getId(), 0), userId)
        );

        return PagedResponse.from(gameResponsePage);
    }

    public GameFieldResponse buildGameFieldResponseForModeratorView(Long gameId, Long userId) {
        Game game = findGameByIdAndHostIdOrModeratorId(gameId, userId);

        List<Ship> ships = shipService.findGameShips(gameId);
        List<Mine> mines = mineService.findGameMines(gameId);
        List<Scan> scans = scanService.findGameScans(gameId);

        return buildGameFieldResponse(game, ships, mines, scans);
    }
    public GameFieldResponse buildGameFieldResponseForPlayerView(Long gameId, Long userId) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);
        Game game = player.getGame();

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

                    int visionRadius = game.getRuleset().getVisionRadius();
                    mines.addAll(mineService.findMinesAroundShip(gameId, visionRadius, min, max));
                });

        return buildGameFieldResponse(game, ships, mines, scans);
    }

    private GameResponse buildGameResponse(Game game, int playersCount, Long currentUserId) {
        GameResponse gameResponse = gameMapper.toResponse(game);
        gameResponse.setPlayersCount(playersCount);

        gameResponse.setJoinedByCurrentUser(
                currentUserId != null
                && playerService.existsByUserAndGame(game.getId(), currentUserId)
        );

        return gameResponse;
    }
    private GameFieldResponse buildGameFieldResponse(Game game, Collection<Ship> ships, Collection<Mine> mines, Collection<Scan> scans) {
        var response = new GameFieldResponse();
        response.setFieldHeight(game.getFieldHeight());
        response.setFieldWidth(game.getFieldWidth());
        response.setShips(ships.stream().map(fieldObjectMapper::toResponse).toList());
        response.setMines(mines.stream().map(fieldObjectMapper::toResponse).toList());
        response.setScans(scans.stream().map(fieldObjectMapper::toResponse).toList());

        return response;
    }


}
