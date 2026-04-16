package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceAlreadyExistsException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.INSUFFICIENT_ENERGY;
import static com.example.aquaticboogaloo.util.EntityConst.*;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;

    public Player createPlayer(Game game, User user) {
        Player player = new Player();
        player.setGame(game);
        player.setUser(user);

        return playerRepository.save(player);
    }

    public void playerShouldNotExist(Long gameId, Long userId) {
        if (playerRepository.existsByUserIdAndGameId(userId, gameId))
            throw new ResourceAlreadyExistsException(PLAYER, USER + ID, userId);
    }

    public Player findPlayerByUserIdAndGameId(Long gameId, Long userId) {
        return playerRepository.findByUserIdAndGameId(userId, gameId)
                .orElseThrow(AccessDeniedException::new);
    }

    public void subtractPlayerEnergy(Long playerId, int energyAmount) {
        int rows = playerRepository.subtractPlayerEnergy(playerId, energyAmount);

        if (rows < 1) throw new BadRequestException(INSUFFICIENT_ENERGY);
    }

    public void addPlayerEnergy(Long playerId, int energyAmount) {
        int rows = playerRepository.addPlayerEnergy(playerId, energyAmount);

        // TODO: if (rows < 1)?
    }
}

