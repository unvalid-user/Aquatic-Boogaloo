package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.*;
import com.example.aquaticboogaloo.dto.response.*;
import com.example.aquaticboogaloo.dto.response.action.*;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.enums.*;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameStateService {

    private final GameResponseService gameResponseService;
    private final PlayerService playerService;
    private final AttackService attackService;
    private final TurnResultService turnResultService;
    private final GameService gameService;

    private final ActionRepository actionRepository;
    private final PlayerRepository playerRepository;

    private final PlayerMapper playerMapper;
    private final ActionMapper actionMapper;

    @Transactional
    public GameStateResponse getState(Long gameId, Long userId) {
        // Serializes this snapshot with planning/turn transitions for a consistent round.
        gameService.findForUpdate(gameId);

        GameResponse game = gameResponseService.buildGameResponseWithPlayersCount(gameId, userId);
        UserInGameInfo membership = playerService.getUserInfoInGame(gameId, userId);
        PlayerResponse player = membership.getPlayer();

        boolean alive = player != null && player.getStatus() != PlayerStatus.DEAD;
        boolean started = game.getStatus() != GameStatus.NEW;
        boolean planning = alive
                && game.getStatus() == GameStatus.ACTIVE
                && player.getStatus() == PlayerStatus.PLANNING;

        GameFieldResponse field = alive && started
                ? gameResponseService.buildGameFieldResponseForPlayerView(gameId, userId)
                : null;
        List<KnownCellResponse> known = alive && started
                ? attackService.getKnownCells(gameId, userId)
                : List.of();
        List<ActionResponse> plan = alive && game.getStatus() == GameStatus.ACTIVE
                ? actionRepository.findByActor_IdAndCreatedAtTurnAndStatus(
                        player.getId(),
                        game.getCurrentTurn(),
                        ActionStatus.PLANNED
                ).stream()
                    .map(actionMapper::toResponse)
                    .toList()
                : List.of();
        Map<ActionType, Integer> bonuses = planning
                ? playerService.findPlayerById(player.getId())
                    .getBonuses()
                    .stream()
                    .collect(Collectors.groupingBy(
                                b -> b.getType(),
                                Collectors.summingInt(b -> b.getQuantity())
                            )
                    )
                : Map.of();
        TurnResultResponse history = player != null && game.getCurrentTurn() > 1
                ? turnResultService.getTurnResultsForPlayerView(gameId, userId, null)
                : null;
        List<PlayerResponse> list = playerRepository.findByGame_Id(gameId, Pageable.unpaged()).stream()
                .map(playerMapper::toResponse)
                .toList();

        return new GameStateResponse(
            game,
            membership,
            gameResponseService.getGameRuleset(gameId),
            field,
            known,
            plan,
            bonuses,
            history,
            list
        );
    }
}
