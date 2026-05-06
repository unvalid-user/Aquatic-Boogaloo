package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.request.ActionRequest;
import com.example.aquaticboogaloo.dto.response.action.ActionCreationResponse;
import com.example.aquaticboogaloo.dto.response.action.ActionResponse;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.ActionService;
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

    @GetMapping
    public List<ActionResponse> getPlannedActions(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return actionService.getPlannedActions(gameId, userId);
    }

    @GetMapping("/bonuses")
    public Map<ActionType, Integer> getBonuses(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return actionService.getBonusActions(gameId, userId);
    }

    @PostMapping
    public ActionCreationResponse createActions(
            @PathVariable Long gameId,
            @CurrentUserId Long userId,
            @RequestBody List<ActionRequest> request
    ) {
        return actionService.createActions(gameId, userId, request);
    }

    @DeleteMapping("/{actionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAction(
            @PathVariable Long gameId,
            @PathVariable Long actionId,
            @CurrentUserId Long userId
    ) {
        actionService.cancelAction(gameId, actionId, userId);
    }

    @PatchMapping("/commit")
    public void commitActions(
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        actionService.endTurn(gameId, userId);
    }
}
