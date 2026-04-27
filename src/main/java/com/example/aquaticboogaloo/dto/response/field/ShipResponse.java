package com.example.aquaticboogaloo.dto.response.field;

import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.enums.ShipType;

import java.util.List;

public class ShipResponse {
    private long id;
    private ShipType type;
    private ShipStatus status;
    private List<ShipCellResponse> shipCells;
}
