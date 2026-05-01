package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.response.PlayerResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.PlayerService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/games/{gameId}/players")
public class PlayersController {
    private final PlayerService playerService;

    public PlayersController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public PagedResponse<PlayerResponse> getAllPlayers(
            Pageable pageable,
            @PathVariable Long gameId
    ) {
        return playerService.getAllPaged(gameId, pageable);
    }

    @GetMapping("/{playerId}")
    public PlayerResponse getById(
            @PathVariable Long gameId,
            @PathVariable Long playerId
    ) {
        return playerService.getById(gameId, playerId);
    }

    @DeleteMapping("/{playerId}")
    public void deleteById(
            @PathVariable Long gameId,
            @PathVariable Long playerId,
            @CurrentUserId Long userId
    ) {
        playerService.deletePlayer(gameId, playerId, userId);
    }
}
