package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.PlayerFilter;
import com.example.aquaticboogaloo.dto.response.PlayerResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/players")
@RequiredArgsConstructor
public class PlayersController {
    private final PlayerService playerService;

    @GetMapping
    public PagedResponse<PlayerResponse> getAllPlayers(
            Pageable pageable,
            @ModelAttribute PlayerFilter filter
            ) {
        return playerService.getAllPaged(filter, pageable);
    }

    @GetMapping("/{playerId}")
    public PlayerResponse getById(
            @PathVariable Long playerId
    ) {
        return playerService.getById(playerId);
    }

    @DeleteMapping("/{playerId}")
    public void deleteById(
            @PathVariable Long playerId,
            @CurrentUserId Long userId
    ) {
        playerService.deletePlayer(playerId, userId);
    }
}
