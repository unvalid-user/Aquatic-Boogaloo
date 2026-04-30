package com.example.aquaticboogaloo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGameRequest {
    @NotBlank
    private String title;
    private String description;
    // TODO: password validation
    private String password;
}
