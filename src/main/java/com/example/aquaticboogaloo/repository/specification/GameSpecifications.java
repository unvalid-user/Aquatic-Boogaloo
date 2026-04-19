package com.example.aquaticboogaloo.repository.specification;

import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Game_;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import org.springframework.data.jpa.domain.Specification;

public class GameSpecifications {
    public static Specification<Game> withFilter(GameFilter filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                withTitleContains(filter.search()),
                withStatus(filter.status()),
                hasPassword(filter.requiresPasswordToJoin())
        );
    }

    private static Specification<Game> withTitleContains(String value) {
        return SpecificationUtils.likeIgnoreCaseIfPresent(Game_.title, value);
    }

    private static Specification<Game> withStatus(GameStatus gameStatus) {
        return SpecificationUtils.equalIfPresent(Game_.status, gameStatus);
    }

    private static Specification<Game> hasPassword(Boolean requiresPasswordToJoin) {
        if (requiresPasswordToJoin == null) return null;

        return (root, query, cb) -> requiresPasswordToJoin
                ? cb.isNotNull(root.get(Game_.passwordHash))
                : cb.isNull(root.get(Game_.passwordHash));
    }
}
