package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.GameLifecycleService;
import com.example.aquaticboogaloo.service.GameResponseService;
import com.example.aquaticboogaloo.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Game controller for host/moderator view"
)
@RestController
@RequestMapping("api/v1/management/games")
@RequiredArgsConstructor
public class GameManagementController {

    private final GameLifecycleService gameLifecycleService;
    private final GameResponseService gameResponseService;

    @GetMapping
    public PagedResponse<GameResponse> getAllPaged(
            Pageable pageable,
            @ModelAttribute GameFilter gameFilter,
            @CurrentUserId Long userId
    ) {
        return gameResponseService.findModeratedGamesPaged(pageable, gameFilter, userId);
    }

    @GetMapping("/{gameId}/field")
    public GameFieldResponse getGameField(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return gameResponseService.buildGameFieldResponseForModeratorView(gameId, userId);
    }

    @GetMapping("/{gameId}/moderators")
    public void getModerators(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        // TODO
    }

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

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        gameLifecycleService.deleteGame(gameId, userId);
    }

    @PatchMapping("/{gameId}/start")
    public void startGame(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        gameLifecycleService.startGame(gameId, userId);
    }

    // for NEW games ONLY
    @PatchMapping("/{gameId}/ruleset")
    public void updateRuleset(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        // TODO
    }

    @PatchMapping("/{gameId}/moderators")
    public void addModerator(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        // TODO
    }

    @DeleteMapping("/{gameId}/moderators")
    public void removeModerator(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        // TODO
    }
}
