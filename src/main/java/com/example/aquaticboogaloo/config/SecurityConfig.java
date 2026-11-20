package com.example.aquaticboogaloo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> discordOAuth2UserService
    ) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/login",
                                "/error",
                                "/api/v1/session",
                                "/api/v1/csrf",
                                "/api/v1/dev/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/games/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/games", "/api/v1/games/{gameId}").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(errors -> errors
                        .defaultAuthenticationEntryPointFor(
                                (request, response, exception) -> {
                                        response.setStatus(401);
                                        response.setContentType("application/json");
                                        response.getWriter().write("{\"message\":\"Authentication required\"}");
                                    },
                        request -> request.getRequestURI().startsWith("/api/")
                        )
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info.userService(discordOAuth2UserService))
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(
                                (request, response, authentication) -> response.setStatus(204)
                        )
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
