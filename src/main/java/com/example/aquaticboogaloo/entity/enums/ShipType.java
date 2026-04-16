package com.example.aquaticboogaloo.entity.enums;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.UNSUPPORTED_SHIP_LENGTH;

public enum ShipType {
    K1(1),
    K2(2),
    K3(3),
    K4(4);

    private final int length;

    ShipType(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    static public ShipType fromLength(int length) {
        for (ShipType type : values()) {
            if (type.length == length)
                return type;
        }
        throw new IllegalArgumentException(String.format(UNSUPPORTED_SHIP_LENGTH, length));
    }
}
