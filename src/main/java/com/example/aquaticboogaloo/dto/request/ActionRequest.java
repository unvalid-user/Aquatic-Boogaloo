package com.example.aquaticboogaloo.dto.request;

import com.example.aquaticboogaloo.entity.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;

public record ActionRequest (
        Integer locationX,
        Integer locationY,
        ActionType type
) {
        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ActionRequest that = (ActionRequest) o;
                return locationX == that.locationX && locationY == that.locationY && type == that.type;
        }

        @Override
        public int hashCode() {
                return Objects.hash(locationX, locationY, type);
        }
}
