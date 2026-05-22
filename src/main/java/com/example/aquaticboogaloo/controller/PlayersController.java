package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.PlayerFilter;
import com.example.aquaticboogaloo.dto.response.PlayerResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Players",
        description = "Player profiles inside games: current player, player lookup, and management removal."
)
@RestController
@RequestMapping("api/v1/players")
@RequiredArgsConstructor
public class PlayersController {
    private final PlayerService playerService;

    @Operation(
            summary = "Get players",
            description = "Returns a paged list of players. Supports filtering"
    )
    @GetMapping
    public PagedResponse<PlayerResponse> getAllPlayers(
            Pageable pageable,
            @ModelAttribute PlayerFilter filter
    ) {
        return playerService.getAllPaged(filter, pageable);
    }

    @Operation(
            summary = "Get current player's profile in a game",
            description = "Returns the Player entity that belongs to the authenticated user in the requested game"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player was not found")
    })
    @GetMapping("/me")
    public PlayerResponse getCurrentPlayer(
            @RequestParam Long gameId,
            @CurrentUserId Long userId
    ) {
        return playerService.getByGameIdAndUserId(gameId, userId);
    }

    @Operation(summary = "Get player by id")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player was not found")
    })
    @GetMapping("/{playerId}")
    public PlayerResponse getById(
            @PathVariable Long playerId
    ) {
        return playerService.getById(playerId);
    }

    @Operation(
            summary = "Remove player from the game",
            description = "Removes a player only while the game is still NEW. Available to game host or moderator."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Game is not in NEW status"),
            @ApiResponse(responseCode = "403", description = "Authenticated user cannot manage this game"),
            @ApiResponse(responseCode = "404", description = "Player was not found")
    })
    @DeleteMapping("/{playerId}")
    public void deleteById(
            @PathVariable Long playerId,
            @CurrentUserId Long userId
    ) {
        playerService.deletePlayer(playerId, userId);
    }
}
