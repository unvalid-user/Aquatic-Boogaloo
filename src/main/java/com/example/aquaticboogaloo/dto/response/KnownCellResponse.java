package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.entity.enums.FieldObjectType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnownCellResponse {
    private int locationX;
    private int locationY;
    private Long attackId;
    private int turn;
    private FieldObjectType objectType;

}
