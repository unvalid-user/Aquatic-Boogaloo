package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.dto.mapper.UserMapper;
import com.example.aquaticboogaloo.dto.response.UserResponse;
import com.example.aquaticboogaloo.security.CurrentUserView;
import com.example.aquaticboogaloo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SessionController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final Environment environment;

    @Value("${aquatic.local-test-login:false}")
    private boolean localTestLogin;

    public record SessionResponse(UserResponse user, boolean localTestLogin) {}

    @GetMapping("/session")
    public SessionResponse session(@AuthenticationPrincipal CurrentUserView principal) {
        return new SessionResponse(
            principal == null
                ? null
                : userMapper.toResponse(userService.findUserById(principal.getUserId())),
            localTestLogin && environment.acceptsProfiles(Profiles.of("local"))
        );
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}
