package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.request.ActionRequest;
import com.example.aquaticboogaloo.dto.response.action.ActionCreationResponse;
import com.example.aquaticboogaloo.dto.response.action.ActionResponse;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.ActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/games/{gameId}/actions")
@RequiredArgsConstructor
public class GameActionController {
    private final ActionService actionService;

    @Operation(
            summary = "Get current player's planned actions"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player not found by game id and user id"),
            @ApiResponse(responseCode = "400", description = "Player status is not PLANNING or game status is not ACTIVE")
    })
    @GetMapping
    public List<ActionResponse> getPlannedActions(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return actionService.getPlannedActions(gameId, userId);
    }

    @Operation(
            summary = "Get current player's bonuses"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player not found by game id and user id"),
            @ApiResponse(responseCode = "400", description = "Player status is not PLANNING or game status is not ACTIVE")
    })
    @GetMapping("/bonuses")
    public Map<ActionType, Integer> getBonuses(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return actionService.getBonusActions(gameId, userId);
    }

    @Operation(
            summary = "Create actions in game",
            description = "Validates and then creates player's actions. Returns list of successfully added Actions" +
                    " and list of Actions that failed validation with cause message"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player not found by game id and user id"),
            @ApiResponse(responseCode = "400", description = "Player status is not PLANNING or game status is not ACTIVE" +
                    " or request body is empty")
    })
    @PostMapping
    public ActionCreationResponse createActions(
            @PathVariable Long gameId,
            @CurrentUserId Long userId,
            @RequestBody List<ActionRequest> request
    ) {
        return actionService.createActions(gameId, userId, request);
    }

    @Operation(
            summary = "Cancel action by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Action not found or player not found by game id and user id"),
            @ApiResponse(responseCode = "400", description = "Player status is not PLANNING or game status is not ACTIVE" +
                    " or action status is not PLANNED")
    })
    @DeleteMapping("/{actionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAction(
            @PathVariable Long gameId,
            @PathVariable Long actionId,
            @CurrentUserId Long userId
    ) {
        actionService.cancelAction(gameId, actionId, userId);
    }

    @Operation(
            summary = "Commit player's actions",
            description = "Changes player status to COMMITED_ACTIONS." +
                    " Player will not be able to create or cancel actions this turn"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Player not found by game id and user id"),
            @ApiResponse(responseCode = "400", description = "Player status is not PLANNING or game status is not ACTIVE" +
                    " or player must spend bonuses first")
    })
    @PatchMapping("/commit")
    public void commitActions(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        actionService.endTurn(gameId, userId);
    }
}
