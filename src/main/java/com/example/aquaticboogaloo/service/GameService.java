package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.request.CreateGameRequest;
import com.example.aquaticboogaloo.dto.mapper.GameMapper;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.GameRepository;
import com.example.aquaticboogaloo.repository.PlayerRepository;
import com.example.aquaticboogaloo.repository.projection.GamePlayersCountProjection;
import com.example.aquaticboogaloo.repository.specification.GameSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.Instant;
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
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final GameMapper gameMapper;
    private final PlayerRepository playerRepository;

    @Transactional
    public Game createGame(CreateGameRequest request, Long userId) {
        User user = userService.findUserById(userId);

        GameRuleset ruleset = new GameRuleset();
        ruleset.setCreatedBy(user);

        Game game = new Game();
        game.setHostUser(user);
        game.setTitle(request.getTitle());
        game.setRuleset(ruleset);
        if (request.getPassword() != null && !request.getPassword().isEmpty())
            game.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return gameRepository.save(game);
    }

    public void deleteGame(Long gameId, Long userId) {
        Game game = findGameByIdAndHostId(gameId, userId);

        if (game.getStatus() != GameStatus.NEW) throw new BadRequestException(WRONG_GAME_STATE);

        gameRepository.delete(game);
    }

    public void updateGameStatus(Long gameId, GameStatus currentStatus, GameStatus newStatus) {
        int rows = gameRepository.updateGameStatus(gameId, currentStatus, newStatus);

        if (rows < 1) throw new BadRequestException(WRONG_GAME_STATE);
    }

    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() ->
                new ResourceNotFoundException(GAME, ID, gameId));
    }

    public GameResponse getGameResponseWithPlayersCount(Long gameId) {
        GameResponse response = gameMapper.toResponse(findGameById(gameId));

        int playersCount = playerRepository.countPlayersByGameIds(List.of(gameId)).getFirst().getPlayersCount();
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
        Map<Long, Integer> gamePlayerCounts = playerRepository.countPlayersByGameIds(gamesPage.map(Game::getId).getContent())
                .stream()
                .collect(Collectors.toMap(
                        GamePlayersCountProjection::getGameId,
                        GamePlayersCountProjection::getPlayersCount
                ));

        var gameResponsePage = gamesPage.map(game -> {
            GameResponse gameResponse = gameMapper.toResponse(game);
            gameResponse.setPlayersCount(gamePlayerCounts.get(game.getId()));

            return gameResponse;
        });

        return PagedResponse.from(gameResponsePage);
    }

}
