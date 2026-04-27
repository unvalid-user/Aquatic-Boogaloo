package com.example.aquaticboogaloo.dto.response.field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldObjectResponse {
    private Long id;
    private long playerId;
    private int locationX;
    private int locationY;
}
