package com.example.aquaticboogaloo.repository.specification;

import com.example.aquaticboogaloo.dto.filter.PlayerFilter;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import org.springframework.data.jpa.domain.Specification;

public class PlayerSpecifications {
    public static Specification<Player> withFilter(PlayerFilter filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                withGameId(filter.gameId()),
                withStatus(filter.status())
        );
    }

    private static Specification<Player> withGameId(Long gameId) {
        return SpecificationUtils.equalIfPresent(
                Player_.game,
                Game_.id,
                gameId
        );
    }

    private static Specification<Player> withStatus(PlayerStatus status) {
        return SpecificationUtils.equalIfPresent(Player_.status, status);
    }
}
