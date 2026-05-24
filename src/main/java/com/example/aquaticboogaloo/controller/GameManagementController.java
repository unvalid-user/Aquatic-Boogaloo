package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.request.AddModeratorRequest;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.UserResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.GameLifecycleService;
import com.example.aquaticboogaloo.service.GameResponseService;
import com.example.aquaticboogaloo.service.GameService;
import com.example.aquaticboogaloo.service.ModeratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Game controller for host/moderator view"
)
@RestController
@RequestMapping("api/v1/management/games")
@RequiredArgsConstructor
public class GameManagementController {

    private final GameLifecycleService gameLifecycleService;
    private final GameResponseService gameResponseService;
    private final ModeratorService moderatorService;

    @Operation(
            summary = "Get games where current user is host or moderator",
            description = "Returns a paged list of games. Supports filtering"
    )
    @GetMapping
    public PagedResponse<GameResponse> getAllPaged(
            Pageable pageable,
            @ModelAttribute GameFilter gameFilter,
            @CurrentUserId Long userId
    ) {
        return gameResponseService.findModeratedGamesPaged(pageable, gameFilter, userId);
    }

    @Operation(
            summary = "Get game field",
            description = "Returns game field with all objects"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Authenticated user is not a moderator or host in this game")
    })
    @GetMapping("/{gameId}/field")
    public GameFieldResponse getGameField(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return gameResponseService.buildGameFieldResponseForModeratorView(gameId, userId);
    }

    @Operation(
            summary = "WIP"
    )
    @GetMapping("/{gameId}/events")
    public void getAllEvents(
            Pageable pageable,
            @PathVariable Long gameId,
            @CurrentUserId Long userId
            // TODO: EventFilter
    ) {
        // TODO
    }

        // for host only

    @Operation(
            summary = "Remove game by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Authenticated user is not the host of this game"),
            @ApiResponse(responseCode = "400", description = "Game status is not NEW")
    })
    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        gameLifecycleService.deleteGame(gameId, userId);
    }

    @Operation(
            summary = "Start game by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Authenticated user is not the host of this game"),
            @ApiResponse(responseCode = "400", description = "Game status is not NEW or not enough players")
    })
    @PatchMapping("/{gameId}/start")
    public void startGame(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        gameLifecycleService.startGame(gameId, userId);
    }

    // for NEW games ONLY
    @Operation(
            summary = "WIP"
    )
    @PatchMapping("/{gameId}/ruleset")
    public void updateRuleset(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        // TODO
    }

    @Operation(
            summary = "Add moderator to the game"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Authenticated user is not the host of this game"),
            @ApiResponse(responseCode = "404", description = "User not found by id"),
            @ApiResponse(responseCode = "400", description = "Host cannot be a moderator")
    })
    @PatchMapping("/{gameId}/moderators")
    public void addModerator(
            @PathVariable Long gameId,
            @ModelAttribute @Valid AddModeratorRequest request,
            @CurrentUserId Long hostId
    ) {
        moderatorService.addGameModerator(gameId, hostId, request);
    }

    @Operation(
            summary = "Remove moderator from the game"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Authenticated user is not the host of this game"),
            @ApiResponse(responseCode = "404", description = "User not found by id")
    })
    @DeleteMapping("/{gameId}/moderators/{userId}")
    public void removeModerator(
            @PathVariable Long gameId,
            @PathVariable Long userId,
            @CurrentUserId Long hostId
    ) {
        moderatorService.removeModerator(gameId, hostId, userId);
    }
}
