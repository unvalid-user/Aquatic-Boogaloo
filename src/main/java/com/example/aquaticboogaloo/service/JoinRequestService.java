package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.filter.JoinRequestFilter;
import com.example.aquaticboogaloo.dto.mapper.JoinRequestMapper;
import com.example.aquaticboogaloo.dto.response.JoinRequestResponse;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.JoinRequestStatus;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ConflictException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.GameJoinRequestRepository;
import com.example.aquaticboogaloo.repository.specification.JoinRequestSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.*;
import static com.example.aquaticboogaloo.util.EntityConst.ID;

@Service
@RequiredArgsConstructor
public class JoinRequestService {

    private final GameJoinRequestRepository joinRequestRepository;
    private final GameService gameService;
    private final JoinRequestMapper joinRequestMapper;
    private final UserService userService;
    private final PlayerService playerService;


    public PagedResponse<JoinRequestResponse> findAllPaged(Pageable pageable, JoinRequestFilter filter, Long userId) {
        gameService.findGameByIdAndHostIdOrModeratorId(filter.gameId(), userId);

        var spec = JoinRequestSpecifications.withFilter(filter);

        var joinRequestsPage = joinRequestRepository.findAll(spec, pageable);

        return PagedResponse.from(joinRequestsPage.map(joinRequestMapper::toResponse));
    }

    public JoinRequestResponse getById(Long userId, Long joinRequestId) {
        GameJoinRequest joinRequest = findById(joinRequestId);
        gameService.findGameByIdAndHostIdOrModeratorId(joinRequest.getGame().getId(), userId);

        return joinRequestMapper.toResponse(joinRequest);
    }

    @Transactional
    public void approveJoinRequest(Long joinRequestId, Long userId) {
        GameJoinRequest joinRequest = findByIdForUpdate(joinRequestId);
        Game game = gameService.findGameByIdAndHostIdOrModeratorId(joinRequest.getGame().getId(), userId);

        if (game.getStatus() != GameStatus.NEW) throw new BadRequestException(WRONG_GAME_STATE);

        Player player = playerService.createPlayer(game, joinRequest.getUser());

        joinRequestRepository.delete(joinRequest);
    }

    @Transactional
    public void rejectJoinRequest(Long joinRequestId, Long userId) {
        GameJoinRequest joinRequest = findByIdForUpdate(joinRequestId);
        gameService.findGameByIdAndHostIdOrModeratorId(joinRequest.getGame().getId(), userId);

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING)
            throw new BadRequestException(WRONG_JOIN_REQUEST_STATUS);

        joinRequest.setStatus(JoinRequestStatus.DENIED);
        joinRequest.setRejectedBy(userService.findUserById(userId));
    }

    private void updateJoinRequestToRejected(Long joinRequestId, Long userId) {
        User user = userService.findUserById(userId);
        int rows = joinRequestRepository.updateJoinRequest(
                joinRequestId, user, JoinRequestStatus.DENIED, JoinRequestStatus.PENDING
        );

        if (rows < 1) throw new ConflictException(FAILED_JOIN_REQUEST_UPDATE);
    }

    private GameJoinRequest findById(Long joinRequestId) {
        return joinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(GameJoinRequest_.class_.getName(), ID, joinRequestId));
    }

    private GameJoinRequest findByIdForUpdate(Long joinRequestId) {
        return joinRequestRepository.findByIdForUpdate(joinRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(GameJoinRequest_.class_.getName(), ID, joinRequestId));
    }


}
