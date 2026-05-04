package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.JoinRequestFilter;
import com.example.aquaticboogaloo.dto.response.JoinRequestResponse;
import com.example.aquaticboogaloo.security.CurrentUserId;
import com.example.aquaticboogaloo.service.JoinRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(("api/v1/management/join-requests"))
@RequiredArgsConstructor
public class JoinRequestManagementController {
    private final JoinRequestService joinRequestService;

    @GetMapping
    public PagedResponse<JoinRequestResponse> getAllPaged(
            Pageable pageable,
            @Valid @ModelAttribute JoinRequestFilter filter,
            @CurrentUserId Long userId
    ) {
        return joinRequestService.findAllPaged(pageable, filter, userId);
    }

    @GetMapping("/{joinRequestId}")
    public JoinRequestResponse getById(
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        return joinRequestService.getById(userId, joinRequestId);
    }

    @PatchMapping("/{joinRequestId}/reject")
    public void rejectJoinRequest(
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        joinRequestService.rejectJoinRequest(joinRequestId, userId);
    }

    @PatchMapping("/{joinRequestId}/approve")
    public void approveJoinRequest(
            @PathVariable Long joinRequestId,
            @CurrentUserId Long userId
    ) {
        joinRequestService.approveJoinRequest(joinRequestId, userId);
    }

}
