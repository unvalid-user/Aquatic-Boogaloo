package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.JoinRequestFilter;
import com.example.aquaticboogaloo.dto.response.JoinRequestResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.JoinRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(("api/v1/management/join-requests"))
@RequiredArgsConstructor
public class JoinRequestManagementController {
    private final JoinRequestService joinRequestService;

    @Operation(
            summary = "Get join requests",
            description = "Returns a paged list of join requests. Supports filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Authenticated user is not a moderator or host in this game")
    })
    @GetMapping
    public PagedResponse<JoinRequestResponse> getAllPaged(
            Pageable pageable,
            @Valid @ModelAttribute JoinRequestFilter filter,
            @CurrentUserId Long userId
    ) {
        return joinRequestService.findAllPaged(pageable, filter, userId);
    }

    @Operation(summary = "Get join request by id")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Authenticated user is not a moderator or host in this game"),
            @ApiResponse(responseCode = "404", description = "Join request was not found")
    })
    @GetMapping("/{joinRequestId}")
    public JoinRequestResponse getById(
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        return joinRequestService.getById(userId, joinRequestId);
    }

    @Operation(summary = "Reject join request by id")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Join request status is not PENDING"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not a moderator or host in this game"),
            @ApiResponse(responseCode = "404", description = "Join request was not found")
    })
    @PatchMapping("/{joinRequestId}/reject")
    public void rejectJoinRequest(
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        joinRequestService.rejectJoinRequest(joinRequestId, userId);
    }

    @Operation(summary = "Approve join request by id")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Game status is not NEW"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not a moderator or host in this game"),
            @ApiResponse(responseCode = "404", description = "Join request was not found")
    })
    @PatchMapping("/{joinRequestId}/approve")
    public void approveJoinRequest(
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        joinRequestService.approveJoinRequest(joinRequestId, userId);
    }

}
