package com.example.aquaticboogaloo.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String discordUserId;
    private String username;
    private String avatarUrl;
}
