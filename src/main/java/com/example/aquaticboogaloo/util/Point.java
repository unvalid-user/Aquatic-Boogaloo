package com.example.aquaticboogaloo.util;

import jakarta.validation.constraints.NotNull;

import java.util.Comparator;

public record Point (
        int x,
        int y

) implements Comparable<Point> {
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Point p) {
            return x == p.x && y == p.y;
        }
        return false;
    }


    @Override
    public int compareTo(@NotNull Point p) {
        return Comparator.comparingInt(Point::x)
                .thenComparingInt(Point::y)
                .compare(this, p);
    }
}
