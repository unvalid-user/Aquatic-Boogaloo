package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.request.CreateGameJoinRequest;
import com.example.aquaticboogaloo.dto.request.CreateGameRequest;
import com.example.aquaticboogaloo.dto.response.*;
import com.example.aquaticboogaloo.dto.response.action.TurnResultResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.security.CurrentUserView;
import com.example.aquaticboogaloo.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(
        name = "Game controller for player view"
)
@RestController
@RequestMapping("api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameJoinService gameJoinService;
    private final GameLifecycleService gameLifecycleService;
    private final GameResponseService gameResponseService;
    private final AttackService attackHitService;
    private final TurnResultService turnResultService;
    private final ModeratorService moderatorService;

    @Operation(summary = "Create new game")
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

    @Operation(summary = "Get game by id")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Game was not found")
    })
    @GetMapping("/{gameId}")
    public GameResponse getGameById(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CurrentUserView currentUser
    ) {
        Long userId = getCurrentUserId(currentUser);
        return gameResponseService.buildGameResponseWithPlayersCount(gameId, userId);
    }

    @Operation(
            summary = "Get games",
            description = "Returns a paged list of games. Supports filtering"
    )
    @GetMapping
    public PagedResponse<GameResponse> getAllPaged(
            Pageable pageable,
            @ModelAttribute GameFilter gameFilter,
            @AuthenticationPrincipal CurrentUserView currentUser
    ) {
        Long userId = getCurrentUserId(currentUser);
        return gameResponseService.findAllPaged(pageable, gameFilter, userId);
    }

    @Operation(
            summary = "Join game",
            description = "Creates a player in the game or a join request. Only for NEW games"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Game was not found"),
            @ApiResponse(responseCode = "409", description = "Player or join request already exists"),
            @ApiResponse(responseCode = "400", description = "Game status is not NEW or wrong password")
    })
    @PostMapping("/{gameId}/join")
    public GameJoinResponse joinGame(
            @PathVariable Long gameId,
            @RequestBody CreateGameJoinRequest joinRequest,
            @CurrentUserId Long userId
    ) {
        return gameJoinService.joinGame(joinRequest, gameId, userId);
    }

    @Operation(
            summary = "Leave game",
            description = "Removes current player from the game. Only for NEW games."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player was not found"),
            @ApiResponse(responseCode = "400", description = "Game status is not NEW")
    })
    @DeleteMapping("/{gameId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGame(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        gameJoinService.leaveGame(gameId, userId);
    }

    @GetMapping("/{gameId}/ruleset")
    public GameRulesetResponse getRuleset(
            @PathVariable Long gameId
    ) {
        return gameResponseService.getGameRuleset(gameId);
    }


    @Operation(
            summary = "Get game moderators list"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Game was not found")
    })
    @GetMapping("/{gameId}/moderators")
    public List<UserResponse> getModerators(
            @PathVariable Long gameId
    ) {
        return moderatorService.getGameModerators(gameId);
    }

    @Operation(
            summary = "Get game field",
            description = "Returns game field objects visible to the current player"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player was not found")
    })
    @GetMapping("/{gameId}/field")
    public GameFieldResponse getGameField(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return gameResponseService.buildGameFieldResponseForPlayerView(gameId, userId);
    }

    @Operation(
            summary = "Get last turn results"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player was not found")
    })
    @GetMapping("/{gameId}/turn-result")
    public TurnResultResponse getTurnResults(
            @PathVariable Long gameId,
            @CurrentUserId Long userId,
            @RequestParam(required = false) Integer turn
    ) {
        return turnResultService.getTurnResults(gameId, userId, turn);
    }

    @Operation(
            summary = "Get known cells",
            description = "Returns cells that have been attacked or mined in previous turns by the current player"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player was not found")
    })
    @GetMapping("/{gameId}/known-cells")
    public List<KnownCellResponse> getKnownCells(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return attackHitService.getKnownCells(gameId, userId);
    }


    private Long getCurrentUserId(CurrentUserView currentUser) {
        return currentUser == null ? null : currentUser.getUserId();
    }

    private URI buildUri(Long id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
