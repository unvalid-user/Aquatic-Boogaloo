package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.GameFilter;
import com.example.aquaticboogaloo.dto.mapper.GameMapper;
import com.example.aquaticboogaloo.dto.mapper.field_object.FieldObjectMapper;
import com.example.aquaticboogaloo.dto.response.GameResponse;
import com.example.aquaticboogaloo.dto.response.field.GameFieldResponse;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.field_objects.Mine;
import com.example.aquaticboogaloo.entity.field_objects.Scan;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.exception.AccessDeniedException;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.GameRepository;
import com.example.aquaticboogaloo.repository.projection.GamePlayersCountProjection;
import com.example.aquaticboogaloo.repository.specification.GameSpecifications;
import com.example.aquaticboogaloo.util.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_GAME_STATE;
import static com.example.aquaticboogaloo.util.EntityConst.GAME;
import static com.example.aquaticboogaloo.util.EntityConst.ID;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;


    public void updateGameStatus(Long gameId, GameStatus currentStatus, GameStatus newStatus) {
        int rows = gameRepository.updateGameStatus(gameId, currentStatus, newStatus);

        if (rows < 1) throw new BadRequestException(WRONG_GAME_STATE);
    }

    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() ->
                new ResourceNotFoundException(GAME, ID, gameId));
    }

    public Game findGameByIdAndHostId(Long gameId, Long userId) {
        return gameRepository.findByIdAndHostUser_Id(gameId, userId)
                .orElseThrow(AccessDeniedException::new);
    }
    public Game findGameByIdAndHostIdOrModeratorId(Long gameId, Long userId) {
        return gameRepository.findGameByIdAndHostIdOrModeratorId(gameId, userId)
                .orElseThrow(AccessDeniedException::new);
    }

    public boolean isUserGameModerator(Long gameId, Long userId) {
        return gameRepository.findGameByIdAndHostIdOrModeratorId(gameId, userId)
                .isPresent();
    }

    public List<Long> getGameIdsWithExpiredTurn() {
        return gameRepository.findGameIdsWithExpiredTurn(Instant.now(), GameStatus.ACTIVE);
    }

    public int getPlayersCountByGameId(Long gameId) {
        return gameRepository.countPlayersByGameIds(List.of(gameId))
                .getFirst()
                .getPlayersCount();
    }

    public Map<Long, Integer> getPlayersCountsByGameIds(List<Long> gameIds) {
        return gameRepository.countPlayersByGameIds(gameIds)
                .stream()
                .collect(Collectors.toMap(
                        GamePlayersCountProjection::getGameId,
                        GamePlayersCountProjection::getPlayersCount
                ));
    }

}
