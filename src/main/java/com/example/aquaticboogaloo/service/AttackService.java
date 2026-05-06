package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.AttackMapper;
import com.example.aquaticboogaloo.dto.response.KnownCellResponse;
import com.example.aquaticboogaloo.entity.AttackHit;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.FieldObjectType;
import com.example.aquaticboogaloo.entity.field_objects.Attack;
import com.example.aquaticboogaloo.repository.AttackHitRepository;
import com.example.aquaticboogaloo.repository.AttackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttackService {

    private final PlayerService playerService;
    private final AttackHitRepository attackHitRepository;
    private final AttackRepository attackRepository;
    private final AttackMapper attackMapper;


    public List<KnownCellResponse> getKnownCells(Long gameId, Long userId) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);

        return attackRepository.findByAction_Actor_Id(player.getId()).stream()
                .filter(attack -> attack.getHit() == null
                        || attack.getHit().getObjectType() == FieldObjectType.SHIP
                        || attack.getHit().getObjectType() == FieldObjectType.MINE)
                .map(attackMapper::toKnownCellResponse)
                .toList();
    }

    public List<Attack> getAttacksByPlayerIdAndTurn(Long playerId, int turn) {
        return attackRepository.findByAction_Actor_IdAndAction_CreatedAtTurn(playerId, turn);
    }

    public List<AttackHit> getAttackHitsByObjectOwnerIdAndTurn(Long playerId, int turn) {
        return attackHitRepository.findByObjectOwner_IdAndAttack_Action_CreatedAtTurn(playerId, turn);
    }
}
