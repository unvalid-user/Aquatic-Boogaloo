package com.example.aquaticboogaloo.event.listener;

import com.example.aquaticboogaloo.event.PlayerCommitedActionsEvent;
import com.example.aquaticboogaloo.service.GameLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GameTurnListener {

    private final GameLifecycleService gameLifecycleService;

    @Async("gameTurnExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPlayerCommitedActions(PlayerCommitedActionsEvent event) {
        gameLifecycleService.resolveTurnIfAllPlayersReady(event.gameId());
    }
}
