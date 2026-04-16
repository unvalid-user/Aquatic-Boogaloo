package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.request.CreateGameRequest;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.GameRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.List;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_GAME_STATE;
import static com.example.aquaticboogaloo.util.EntityConst.GAME;
import static com.example.aquaticboogaloo.util.EntityConst.ID;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

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

    public void updateGameStatus(Long gameId, GameStatus currentStatus, GameStatus newStatus) {
        int rows = gameRepository.updateGameStatus(gameId, currentStatus, newStatus);

        if (rows < 1) throw new BadRequestException(WRONG_GAME_STATE);
    }

    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() ->
                new ResourceNotFoundException(GAME, ID, gameId));
    }

    public Game findGameByIdAndHostId(Long gameId, Long userId) {
        return gameRepository.findByIdAndHostUserId(gameId, userId)
                .orElseThrow(AccessDeniedException::new);
    }

    public List<Long> getGameIdsWithExpiredTurn() {
        return gameRepository.getGameIdsWithExpiredTurn(Instant.now(), GameStatus.ACTIVE);
    }

}
