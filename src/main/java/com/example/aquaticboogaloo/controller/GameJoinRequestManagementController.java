package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.response.JoinRequestResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(("api/v1/management/games/{gameId}/join-requests"))
@RequiredArgsConstructor
public class GameJoinRequestManagementController {
    private final JoinRequestService joinRequestService;

    @GetMapping
    public PagedResponse<JoinRequestResponse> getAllPaged(
            Pageable pageable,
            @PathVariable Long gameId,
            @CurrentUserId Long userId
    ) {
        return joinRequestService.findAllPaged(pageable, gameId, userId);
    }

    @GetMapping("/{joinRequestId}")
    public JoinRequestResponse getById(
            @PathVariable Long gameId,
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        return joinRequestService.getById(gameId, userId, joinRequestId);
    }

    @PatchMapping("/{joinRequestId}/reject")
    public void rejectJoinRequest(
            @PathVariable Long gameId,
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        joinRequestService.rejectJoinRequest(joinRequestId, gameId, userId);
    }

    @PatchMapping("/{joinRequestId}/approve")
    public void approveJoinRequest(
            @PathVariable Long gameId,
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        joinRequestService.approveJoinRequest(joinRequestId, gameId, userId);
    }

}
