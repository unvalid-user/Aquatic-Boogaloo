package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.request.CreateGameJoinRequest;
import com.example.aquaticboogaloo.dto.request.CreateGameRequest;
import com.example.aquaticboogaloo.dto.response.GameJoinResponse;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.GameJoinService;
import com.example.aquaticboogaloo.service.GameLifecycleService;
import com.example.aquaticboogaloo.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(
        name = "Game controller for player view"
)
@RestController
@RequestMapping("api/v1/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final GameJoinService gameJoinService;
    private final GameLifecycleService gameLifecycleService;

    @PostMapping
    public ResponseEntity<Void> createGame(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateGameRequest createGameRequest
    ) {
        Game game = gameLifecycleService.createGame(createGameRequest, userId);
        return ResponseEntity
                .created(buildUri(game.getId()))
                .build();
    }


    @GetMapping("/{gameId}")
    public GameResponse getGameById(
            @PathVariable Long gameId
    ) {
        return gameService.buildGameResponseWithPlayersCount(gameId);
    }

    @GetMapping
    public PagedResponse<GameResponse> getAllPaged(
            Pageable pageable,
            @ModelAttribute GameFilter gameFilter
    ) {
        return gameService.findAllPaged(pageable, gameFilter);
    }

    @GetMapping("/{gameId}/players")
    public void getAllPlayers(
            @PathVariable Long gameId
    ) {
        // TODO
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

    // only current player's objects
    @GetMapping("/{gameId}/field")
    public GameFieldResponse getGameField(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return gameService.buildGameFieldResponseForPlayerView(gameId, userId);
    }


    private URI buildUri(Long id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
