package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.mapper.GameMapper;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.field_objects.Mine;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.GameRepository;
import com.example.aquaticboogaloo.repository.PlayerRepository;
import com.example.aquaticboogaloo.repository.projection.GamePlayersCountProjection;
import com.example.aquaticboogaloo.repository.specification.GameSpecifications;
import com.example.aquaticboogaloo.util.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final MineService mineService;

    public void updateGameStatus(Long gameId, GameStatus currentStatus, GameStatus newStatus) {
        int rows = gameRepository.updateGameStatus(gameId, currentStatus, newStatus);

        if (rows < 1) throw new BadRequestException(WRONG_GAME_STATE);
    }

    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() ->
                new ResourceNotFoundException(GAME, ID, gameId));
    }

    public GameResponse buildGameResponseWithPlayersCount(Long gameId) {
        GameResponse response = gameMapper.toResponse(findGameById(gameId));

        int playersCount = playerService.getPlayersCountByGameId(gameId);
        response.setPlayersCount(playersCount);

        return response;
    }

    public Game findGameByIdAndHostId(Long gameId, Long userId) {
        return gameRepository.findByIdAndHostUserId(gameId, userId)
                .orElseThrow(AccessDeniedException::new);
    }

    public List<Long> getGameIdsWithExpiredTurn() {
        return gameRepository.findGameIdsWithExpiredTurn(Instant.now(), GameStatus.ACTIVE);
    }

    public PagedResponse<GameResponse> findAllPaged(Pageable pageable, GameFilter gameFilter, Long userId) {
        var specs = GameSpecifications.withFilter(gameFilter, userId);
        return findAllPaged(pageable, specs);
    }
    public PagedResponse<GameResponse> findAllPaged(Pageable pageable, GameFilter gameFilter) {
        var specs = GameSpecifications.withFilter(gameFilter);
        return findAllPaged(pageable, specs);
    }
    private PagedResponse<GameResponse> findAllPaged(Pageable pageable, Specification<Game> specs) {
        Page<Game> gamesPage = gameRepository.findAll(specs, pageable);
        var gamePlayersCounts = playerService.getPlayersCountsByGameIds(gamesPage.map(Game::getId).getContent());

        var gameResponsePage = gamesPage.map(game -> {
            GameResponse gameResponse = gameMapper.toResponse(game);
            gameResponse.setPlayersCount(gamePlayersCounts.get(game.getId()));

            return gameResponse;
        });

        return PagedResponse.from(gameResponsePage);
    }

    public GameFieldResponse buildGameFieldResponse(Long gameId, Long userId) {
        return null;
    }
    public GameFieldResponse buildGameFieldResponseForPlayerView(Long gameId, Long userId) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);
        Game game = player.getGame();

        var response = new GameFieldResponse();
        response.setFieldHeight(game.getFieldHeight());
        response.setFieldWidth(game.getFieldWidth());

        List<Ship> ships = player.getShips();
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

                    mineService.findMinesAroundShip(gameId, game.getRuleset().getVisionRadius(), min, max);
                    // WIP
                });

        return null;
    }


}
