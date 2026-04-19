package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.request.CreateGameJoinRequest;
import com.example.aquaticboogaloo.dto.request.CreateGameRequest;
import com.example.aquaticboogaloo.dto.response.GameJoinResponse;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.mapper.GameMapper;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.GameJoinService;
import com.example.aquaticboogaloo.service.GameLifecycleService;
import com.example.aquaticboogaloo.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final GameMapper gameMapper;
    private final GameJoinService gameJoinService;
    private final GameLifecycleService gameLifecycleService;

    @PostMapping
    public ResponseEntity<Void> createGame(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateGameRequest createGameRequest
    ) {
        Game game = gameService.createGame(createGameRequest, userId);
        return ResponseEntity
                .created(buildUri(game.getId()))
                .build();
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(
            @CurrentUserId Long userId,
            @PathVariable Long gameId
    ) {
        gameService.deleteGame(gameId, userId);
    }

    // TODO:
    //  + add/remove moderators
    //  + PatchMapping for game/ruleset


    @GetMapping("/{gameId}")
    public GameResponse getGameById(
            @PathVariable Long gameId
    ) {
        return gameService.getGameResponseWithPlayersCount(gameId);
    }

    @GetMapping
    public PagedResponse<GameResponse> getAllPaged(
            @PageableDefault Pageable pageable,
            @ModelAttribute GameFilter gameFilter
    ) {
        return gameService.findAllPaged(pageable, gameFilter);
    }

    // TODO: test
    @PostMapping("/{gameId}/join")
    public GameJoinResponse joinGame(
            @PathVariable Long gameId,
            @RequestBody CreateGameJoinRequest joinRequest,
            @CurrentUserId Long userId
    ) {
        return gameJoinService.joinGame(joinRequest, gameId, userId);
    }

    @PatchMapping("/{gameId}/start")
    public void startGame(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        gameLifecycleService.startGame(gameId, userId);
    }

    @GetMapping("/{gameId}/field")
    public void getGameField(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        // TODO
        // only current user's objects
    }


    private URI buildUri(Long id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
