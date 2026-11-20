package com.example.aquaticboogaloo.controller;

import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.repository.UserRepository;
import com.example.aquaticboogaloo.security.OAuth2UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/** Test identities use the same repositories and game services as Discord users. */
@Profile("local")
@ConditionalOnProperty(name = "aquatic.local-test-login", havingValue = "true")
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class LocalTestController {

    private final UserRepository users;

    public record LoginRequest(@NotNull @Pattern(regexp = "[a-zA-Z0-9_-]{2,24}") String username) {}

    @PostMapping("/login")
    public void login(
        @Valid @RequestBody LoginRequest body,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String discordId = "local:" + body.username();
        User user = users.findByDiscordUserId(discordId).orElseGet(() -> new User(discordId));
        user.setUsername(body.username());
        user = users.save(user);
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var principal = new OAuth2UserPrincipal(user, authorities, Map.of("id", discordId), "id");
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, authorities)
        );
        request.getSession();
        request.changeSessionId();
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
    }
}
