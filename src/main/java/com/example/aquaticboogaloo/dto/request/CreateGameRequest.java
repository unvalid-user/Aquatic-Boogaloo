package com.example.aquaticboogaloo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGameRequest {

    @NotBlank
    private String title;

    private String description;
    // TODO: password validation
    private String password;

    @Min(1)
    @Max(100)
    private int turns = 10;

    @Min(30)
    @Max(604800)
    private int turnSeconds = 86400;

    private boolean requestToJoin = true;
    private boolean forceNextTurn = true;
}
