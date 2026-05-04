package com.example.aquaticboogaloo.repository.specification;

import com.example.aquaticboogaloo.dto.filter.JoinRequestFilter;
import com.example.aquaticboogaloo.entity.GameJoinRequest;
import com.example.aquaticboogaloo.entity.GameJoinRequest_;
import com.example.aquaticboogaloo.entity.Game_;
import com.example.aquaticboogaloo.entity.User_;
import com.example.aquaticboogaloo.entity.enums.JoinRequestStatus;
import org.springframework.data.jpa.domain.Specification;

public class JoinRequestSpecifications {
    public static Specification<GameJoinRequest> withFilter(JoinRequestFilter filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                withGameId(filter.gameId()),
                withUsernameContains(filter.username()),
                withStatus(filter.status())
        );
    }

    private static Specification<GameJoinRequest> withUsernameContains(String username) {
        return SpecificationUtils.likeIgnoreCaseIfPresent(
                GameJoinRequest_.user,
                User_.username,
                username
        );
    }

    private static Specification<GameJoinRequest> withGameId(Long gameId) {
        return SpecificationUtils.equalIfPresent(
                GameJoinRequest_.game,
                Game_.id,
                gameId
        );
    }

    private static Specification<GameJoinRequest> withStatus(JoinRequestStatus status) {
        return SpecificationUtils.equalIfPresent(GameJoinRequest_.status, status);
    }
}
