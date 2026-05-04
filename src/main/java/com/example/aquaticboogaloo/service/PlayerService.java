package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.PlayerFilter;
import com.example.aquaticboogaloo.dto.mapper.PlayerMapper;
import com.example.aquaticboogaloo.dto.response.PlayerResponse;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.Player_;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceAlreadyExistsException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.PlayerRepository;
import com.example.aquaticboogaloo.repository.specification.PlayerSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.INSUFFICIENT_ENERGY;
import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_GAME_STATE;
import static com.example.aquaticboogaloo.util.EntityConst.*;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final GameService gameService;

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

    // TODO: race condition (Game status)
    public void deletePlayer(Long playerId, Long userId) {
        Player player = findPlayerById(playerId);
        Game game = gameService.findGameByIdAndHostIdOrModeratorId(player.getGame().getId(), userId);

        if (game.getStatus() != GameStatus.NEW) throw new BadRequestException(WRONG_GAME_STATE);

        playerRepository.delete(player);
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
                .orElseThrow(AccessDeniedException::new);
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

        // TODO: if (rows < 1)?
    }

    public int countPlanningPlayersByGameId(Long gameId) {
        return playerRepository.countByGame_IdAndStatus(gameId, PlayerStatus.PLANNING);
    }
}

