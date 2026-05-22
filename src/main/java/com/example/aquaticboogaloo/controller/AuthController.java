package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.mapper.UserMapper;
import com.example.aquaticboogaloo.dto.response.UserResponse;
import com.example.aquaticboogaloo.security.OAuth2UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

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
}
