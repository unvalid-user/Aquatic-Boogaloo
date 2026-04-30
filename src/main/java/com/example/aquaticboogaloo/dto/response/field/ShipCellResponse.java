package com.example.aquaticboogaloo.dto.response.field;

import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipCellResponse {
    private long id;
    private int locationX;
    private int locationY;
    private boolean isDestroyed;
}
