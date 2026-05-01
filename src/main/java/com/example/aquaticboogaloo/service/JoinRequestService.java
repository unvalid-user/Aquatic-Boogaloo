package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.PagedResponse;
import com.example.aquaticboogaloo.dto.mapper.JoinRequestMapper;
import com.example.aquaticboogaloo.dto.response.JoinRequestResponse;
import com.example.aquaticboogaloo.entity.*;
import com.example.aquaticboogaloo.entity.enums.JoinRequestStatus;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ConflictException;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.GameJoinRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.FAILED_JOIN_REQUEST_UPDATE;
import static com.example.aquaticboogaloo.exception.ExceptionMessage.WRONG_JOIN_REQUEST_STATUS;
import static com.example.aquaticboogaloo.util.EntityConst.ID;

@Service
@RequiredArgsConstructor
public class JoinRequestService {

    private final GameJoinRequestRepository joinRequestRepository;
    private final GameService gameService;
    private final JoinRequestMapper joinRequestMapper;
    private final UserService userService;
    private final PlayerService playerService;


    public PagedResponse<JoinRequestResponse> findAllPaged(Pageable pageable, Long gameId, Long userId) {
        gameService.findGameByIdAndHostIdOrModeratorId(gameId, userId);

        var joinRequestsPaged = joinRequestRepository.findByGame_Id(gameId, pageable);

        return PagedResponse.from(joinRequestsPaged.map(joinRequestMapper::toResponse));
    }

    @Transactional
    public void approveJoinRequest(Long joinRequestId, Long gameId, Long userId) {
        Game game = gameService.findGameByIdAndHostIdOrModeratorId(gameId, userId);
        GameJoinRequest joinRequest = findJoinRequestByIdAndGameIdForUpdate(joinRequestId, gameId);

        Player player = playerService.createPlayer(game, joinRequest.getUser());

        joinRequestRepository.delete(joinRequest);
    }

    @Transactional
    public void rejectJoinRequest(Long joinRequestId, Long gameId, Long userId) {
        gameService.findGameByIdAndHostIdOrModeratorId(gameId, userId);
        GameJoinRequest joinRequest = findJoinRequestByIdAndGameIdForUpdate(joinRequestId, gameId);

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

    public JoinRequestResponse getById(Long gameId, Long userId, Long joinRequestId) {
        gameService.findGameByIdAndHostIdOrModeratorId(gameId, userId);
        GameJoinRequest joinRequest = findJoinRequestByIdAndGameId(joinRequestId, gameId);

        return joinRequestMapper.toResponse(joinRequest);
    }

    private GameJoinRequest findJoinRequestByIdAndGameId(Long joinRequestId, Long gameId) {
        return joinRequestRepository.findByIdAndGame_Id(joinRequestId, gameId)
                .orElseThrow(() -> new ResourceNotFoundException(GameJoinRequest_.class_.getName(), ID, joinRequestId));
    }

    private GameJoinRequest findJoinRequestByIdAndGameIdForUpdate(Long joinRequestId, Long gameId) {
        return joinRequestRepository.findByIdAndGameIdForUpdate(joinRequestId, gameId)
                .orElseThrow(() -> new ResourceNotFoundException(GameJoinRequest_.class_.getName(), ID, joinRequestId));
    }


}
