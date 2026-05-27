package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.PlayerFilter;
import com.example.aquaticboogaloo.dto.mapper.PlayerMapper;
import com.example.aquaticboogaloo.dto.response.PlayerResponse;
import com.example.aquaticboogaloo.dto.response.UserInGameInfo;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.Player_;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.exception.*;
import com.example.aquaticboogaloo.repository.PlayerRepository;
import com.example.aquaticboogaloo.repository.specification.PlayerSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.*;
import static com.example.aquaticboogaloo.util.EntityConst.*;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final GameService gameService;
    private final UserService userService;

    public Player createPlayer(Game game, User user) {
        // TODO: lock game?

        Player player = new Player();
        player.setGame(game);
        player.setUser(user);

        return playerRepository.save(player);
    }

    public PagedResponse<PlayerResponse> getAllPaged(PlayerFilter filter, Pageable pageable) {
        var spec = PlayerSpecifications.withFilter(filter);

        var playersPage = playerRepository.findAll(spec, pageable);

        return PagedResponse.from(playersPage.map(playerMapper::toResponse));
    }

    public PlayerResponse getById(Long playerId) {
        Player player = findPlayerById(playerId);

        return playerMapper.toResponse(player);
    }

    public PlayerResponse getByGameIdAndUserId(Long gameId, Long userId) {
        return playerMapper.toResponse(findPlayerByGameIdAndUserId(gameId, userId));
    }

    @Transactional
    public void deletePlayer(Long playerId, Long userId) {
        Player player = findPlayerById(playerId);
        Game game = gameService.findGameByIdAndHostIdOrModeratorId(player.getGame().getId(), userId);

        if (game.getStatus() != GameStatus.NEW) throw new BadRequestException(WRONG_GAME_STATE);

        removePlayerById(playerId);
    }


    public Player findPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(Player_.class_.getName(), ID, playerId));
    }

    public void playerShouldNotExist(Long gameId, Long userId) {
        if (playerRepository.existsByUser_IdAndGame_Id(userId, gameId))
            throw new ResourceAlreadyExistsException(PLAYER, USER + ID, userId);
    }

    public boolean existsByUserAndGame(Long gameId, Long userId) {
        return playerRepository.existsByUser_IdAndGame_Id(userId, gameId);
    }

    public Player findPlayerByGameIdAndUserId(Long gameId, Long userId) {
        return playerRepository.findByUser_IdAndGame_Id(userId, gameId)
                .orElseThrow(() -> new ResourceNotFoundException(Player_.class_.getName(), USER + ID, userId));
    }

    public Player findPlayerByIdAndGameId(Long gameId, Long playerId) {
        return playerRepository.findByIdAndGame_Id(playerId, gameId)
                .orElseThrow(() -> new ResourceNotFoundException(Player_.class_.getName(), ID, playerId));
    }

    public void subtractPlayerEnergy(Long playerId, int energyAmount) {
        int rows = playerRepository.subtractPlayerEnergy(playerId, energyAmount);

        if (rows < 1) throw new BadRequestException(INSUFFICIENT_ENERGY);
    }

    public void addPlayerEnergy(Long playerId, int energyAmount) {
        int rows = playerRepository.addPlayerEnergy(playerId, energyAmount);

        if (rows < 1) throw new ConflictException(FAILED_UPDATE_PLAYER.formatted(playerId));
    }

    public int countPlanningPlayersByGameId(Long gameId) {
        return playerRepository.countByGame_IdAndStatus(gameId, PlayerStatus.PLANNING);
    }

    public void removePlayerById(Long playerId) {
        int rows = playerRepository.deleteByIdAndGameStatus(playerId, GameStatus.NEW);

        if (rows < 1) throw new ConflictException(FAILED_REMOVE_PLAYER.formatted(playerId));
    }

    public UserInGameInfo getUserInfoInGame(Long gameId, Long userId) {
        Game game = gameService.findGameById(gameId);
        User user = userService.findUserById(userId);

        UserInGameInfo info = new UserInGameInfo();

        info.setHost(game.getHostUser() == user);
        info.setModerator(game.getModerators().contains(user));

        playerRepository.findByUser_IdAndGame_Id(userId, gameId).ifPresent(value ->
                info.setPlayer(playerMapper.toResponse(value)));

        return info;
    }
}

