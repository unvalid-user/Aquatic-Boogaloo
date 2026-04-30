package com.example.aquaticboogaloo.dto.response.field;

import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.enums.ShipType;
import lombok.Data;

import java.util.List;

@Data
public class ShipResponse {
    private long id;
    private long playerId;
    private ShipType type;
    private ShipStatus status;
    private List<ShipCellResponse> shipCells;
}
