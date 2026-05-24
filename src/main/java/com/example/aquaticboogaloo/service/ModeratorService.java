package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.UserMapper;
import com.example.aquaticboogaloo.dto.request.AddModeratorRequest;
import com.example.aquaticboogaloo.dto.response.UserResponse;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.exception.BadRequestException;
import com.example.aquaticboogaloo.exception.ConflictException;
import com.example.aquaticboogaloo.exception.ResourceAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.HOST_CAN_NOT_BE_MODERATOR;

@Service
@RequiredArgsConstructor
public class ModeratorService {

    private final GameService gameService;
    private final UserMapper userMapper;
    private final UserService userService;


    public List<UserResponse> getGameModerators(Long gameId) {
        Game game = gameService.findGameById(gameId);

        return game.getModerators().stream().map(userMapper::toResponse).toList();
    }

    @Transactional
    public void addGameModerator(Long gameId, Long hostId, AddModeratorRequest request) {
        Game game = gameService.findGameByIdAndHostId(gameId, hostId);
        User user = userService.findUserById(request.userId());

        if (game.getHostUser().equals(user)) throw new BadRequestException(HOST_CAN_NOT_BE_MODERATOR);

        game.getModerators().add(user);
    }

    @Transactional
    public void removeModerator(Long gameId, Long hostId, Long userId) {
        Game game = gameService.findGameByIdAndHostId(gameId, hostId);
        User user = userService.findUserById(userId);

        game.getModerators().remove(user);
    }

}
