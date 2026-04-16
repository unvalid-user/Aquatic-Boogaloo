package com.example.aquaticboogaloo.service.resolve_turn;

import com.example.aquaticboogaloo.service.GameLifecycleService;
import com.example.aquaticboogaloo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final GameService gameService;
    private final GameLifecycleService gameLifecycleService;

    @Scheduled(fixedDelay = 600000)
    public void tick() {
        // TODO: concurrent processing
        // + exception handling
        List<Long> gameIds = gameService.getGameIdsWithExpiredTurn();

        gameIds.forEach(gameLifecycleService::resolveTurn);
    }
}
