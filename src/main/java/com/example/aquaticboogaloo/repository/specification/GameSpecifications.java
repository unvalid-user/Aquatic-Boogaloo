package com.example.aquaticboogaloo.repository.specification;

import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Game_;
import com.example.aquaticboogaloo.entity.Player_;
import com.example.aquaticboogaloo.entity.User_;
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
    public static Specification<Game> withFilterAndModeratedByUser(GameFilter filter, Long userId) {
        if (userId == null) return null;

        return withFilter(filter).and(withHostId(userId).or(withModeratorId(userId)));
    }

    public static Specification<Game> withFilterAndJoinedByUser(GameFilter filter, Long userId) {
        if (userId == null) return null;

        return withFilter(filter).and(withUserJoined(userId));
    }

    private static Specification<Game> withUserJoined(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.join(Game_.players).get(Player_.user).get(User_.id), userId);
    }

    private static Specification<Game> withModeratorId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.join(Game_.moderators).get(User_.id), userId);
    }

    private static Specification<Game> withHostId(Long userId) {
        return SpecificationUtils.equalIfPresent(Game_.hostUser, User_.id, userId);
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
