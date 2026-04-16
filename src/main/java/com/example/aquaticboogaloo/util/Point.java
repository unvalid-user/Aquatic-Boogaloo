package com.example.aquaticboogaloo.util;

public record Point (
        int x,
        int y
) {


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Point p) {
            return x == p.x && y == p.y;
        }
        return false;
    }
}
