package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.mapper.UserMapper;
import com.example.aquaticboogaloo.dto.response.UserResponse;
import com.example.aquaticboogaloo.security.OAuth2UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;


    @Operation(
            summary = "Get current user info"
    )
    @GetMapping("/me")
    public UserResponse me(
            @AuthenticationPrincipal OAuth2UserPrincipal principal
    ) {
        return userMapper.toResponse(principal.getUser());
    }


    @Operation(
            summary = "WIP"
    )
    @GetMapping
    public PagedResponse<UserResponse> getAll(
    ) {
        // TODO
        return null;
    }
}
