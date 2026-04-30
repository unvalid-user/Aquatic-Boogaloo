package com.example.aquaticboogaloo.dto.response.field;

import com.example.aquaticboogaloo.entity.Action;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldObjectResponse {
        private Long objectId;
        private Long playerId;
        private int locationX;
        private int locationY;
}
