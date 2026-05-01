package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.request.CreateGameJoinRequest;
import com.example.aquaticboogaloo.dto.response.GameJoinResponse;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.GameJoinRequest;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceAlreadyExistsException;
import com.example.aquaticboogaloo.repository.GameJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_GAME_PASSWORD;
import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_GAME_STATE;
import static com.example.aquaticboogaloo.util.EntityConst.*;

@Service
@RequiredArgsConstructor
public class GameJoinService {

    private final GameJoinRequestRepository joinRequestRepository;
    private final PlayerService playerService;
    private final GameService gameService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public GameJoinRequest createJoinRequest(Game game, User user) {
        GameJoinRequest gameJoinRequest = new GameJoinRequest();
        gameJoinRequest.setGame(game);
        gameJoinRequest.setUser(user);

        return joinRequestRepository.save(gameJoinRequest);
    }

    public GameJoinResponse joinGame(CreateGameJoinRequest request, Long gameId, Long userId) {
        Game game = gameService.findGameById(gameId);
        User user = userService.findUserById(userId);

        playerService.playerShouldNotExist(gameId, userId);
        joinRequestShouldNotExist(gameId, userId);

        if (game.getStatus() != GameStatus.NEW)
            throw new BadRequestException(WRONG_GAME_STATE);

        if (game.getPasswordHash() != null && !passwordMatches(request.getPassword(), game.getPasswordHash()))
            throw new BadRequestException(WRONG_GAME_PASSWORD);

        var response = new GameJoinResponse();
        if (!gameService.isUserGameModerator(gameId, userId) && game.isRequestToJoin()) {
            GameJoinRequest gameJoinRequest = createJoinRequest(game, user);
            response.setResult(GameJoinResponse.JoinGameResult.PENDING_APPROVAL);
            response.setJoinRequestId(gameJoinRequest.getId());
        } else {
            Player player = playerService.createPlayer(game, user);
            response.setResult(GameJoinResponse.JoinGameResult.JOINED);
            response.setPlayerId(player.getId());
        }

        return response;
    }

    public void joinRequestShouldNotExist(Long gameId, Long userId) {
        if (joinRequestRepository.existsByUserIdAndGameId(userId, gameId))
            throw new ResourceAlreadyExistsException(GAME_JOIN_REQUEST, USER + ID, userId);
    }

    private boolean passwordMatches(String rawPassword, String passwordHash) {
        return rawPassword != null && passwordEncoder.matches(rawPassword, passwordHash);
    }
}
