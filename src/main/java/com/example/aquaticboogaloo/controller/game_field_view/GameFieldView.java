package com.example.aquaticboogaloo.controller.game_field_view;

import java.util.List;

public record GameFieldView(
        int width,
        int height,
        List<ShipView> ships
) {
}
