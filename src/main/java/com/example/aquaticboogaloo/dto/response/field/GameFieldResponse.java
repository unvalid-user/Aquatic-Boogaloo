package com.example.aquaticboogaloo.dto.response.field;

import com.example.aquaticboogaloo.dto.response.KnownCellResponse;
import lombok.Data;

import java.util.List;

@Data
public class GameFieldResponse {
    private int fieldWidth;
    private int fieldHeight;

    private List<ShipResponse> ships;
    private List<MineResponse> mines;
    private List<ScanResponse> scans;
    private List<KnownCellResponse> attacks;
}
