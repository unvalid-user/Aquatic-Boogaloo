package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.dto.mapper.AttackMapper;
import com.example.aquaticboogaloo.dto.response.KnownCellResponse;
import com.example.aquaticboogaloo.entity.AttackHit;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.AttackHitImpact;
import com.example.aquaticboogaloo.entity.enums.FieldObjectType;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.field_objects.Attack;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import com.example.aquaticboogaloo.repository.AttackHitRepository;
import com.example.aquaticboogaloo.repository.AttackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttackService {

    private final PlayerService playerService;
    private final AttackHitRepository attackHitRepository;
    private final AttackRepository attackRepository;
    private final AttackMapper attackMapper;

    @Transactional(readOnly = true)
    public List<KnownCellResponse> getKnownCells(Long gameId, Long userId) {
        Player player = playerService.findPlayerByGameIdAndUserId(gameId, userId);
//        if (player.getStatus() == PlayerStatus.DEAD) {
//            throw new AccessDeniedException();
//        }

        var known = new LinkedHashMap<String, KnownCellResponse>();

        // get player's attacks that hit ship, mine or miss
        var attacks = attackRepository.findByAction_Actor_Id(player.getId())
                .stream()
                .filter(attack ->
                        attack.getHit() == null ||
                        attack.getHit().getObjectType() == FieldObjectType.SHIP ||
                        attack.getHit().getObjectType() == FieldObjectType.MINE
                )
                .sorted(Comparator.comparingInt((Attack a) ->
                                a.getAction().getCreatedAtTurn()
                        ).thenComparing(Attack::getId)
                )
                .toList();

        // add destroyed ships radius to known cells
        for (Attack attack : attacks) {
            AttackHit hit = attack.getHit();
            if (
                    hit == null ||
                    hit.getObjectType() != FieldObjectType.SHIP ||
                    hit.getHitImpact() != AttackHitImpact.DESTROYED ||
                    hit.getHitShipCell() == null
            ) continue;

            for (ShipCell cell : hit.getHitShipCell().getShip().getShipCells()) {
                for (int x = cell.getLocationX() - 1; x <= cell.getLocationX() + 1; x++) {
                    for (int y = cell.getLocationY() - 1; y <= cell.getLocationY() + 1; y++) {
                        if (
                                x < 0 ||
                                y < 0 ||
                                x >= player.getGame().getFieldWidth() ||
                                y >= player.getGame().getFieldHeight()
                        ) continue;

                        putKnownCell(known, attack, x, y, null);
                    }
                }
            }
            for (ShipCell cell : hit.getHitShipCell().getShip().getShipCells()) {
                putKnownCell(
                        known,
                        attack,
                        cell.getLocationX(),
                        cell.getLocationY(),
                        FieldObjectType.SHIP
                );
            }
        }
        // Actual attack evidence takes precedence over inferred empty neighbours.
        for (Attack attack : attacks) {
            KnownCellResponse cell = attackMapper.toKnownCellResponse(attack);
            String location = cell.getLocationX() + ":" + cell.getLocationY();
            if (
                    known.containsKey(location) &&
                    known.get(location).getObjectType() == FieldObjectType.SHIP
            ) {
                continue;
            }
            known.put(location, cell);
        }
        return List.copyOf(known.values());
    }

    private void putKnownCell(
            Map<String, KnownCellResponse> known,
            Attack attack,
            int x,
            int y,
            FieldObjectType objectType
    ) {
        KnownCellResponse cell = new KnownCellResponse();
        cell.setLocationX(x);
        cell.setLocationY(y);
        cell.setAttackId(attack.getId());
        cell.setTurn(attack.getAction().getCreatedAtTurn());
        cell.setObjectType(objectType);

        known.put(x + ":" + y, cell);
    }

    public List<KnownCellResponse> getEnemiesAttacksInVisionRadius(Player player) {
        Game game = player.getGame();
        final int visionRadius = game.getRuleset().getVisionRadius();
        List<Ship> ships = player.getShips();

        // TODO: filtering on repository side?
        return attackRepository.findByAction_CreatedAtTurn(game.getCurrentTurn() - 1).stream()
                .filter(a -> a.getAction().getActor().getGame().getId().equals(game.getId()))
                .filter(a -> !a.getAction().getActor().getId().equals(player.getId()))
                .filter(a -> ships.stream()
                        // TODO: ? destroyed status in current turn
                        .filter(s -> s.getStatus() != ShipStatus.DESTROYED)
                        .flatMap(s -> s.getShipCells().stream())
                        .anyMatch(c ->
                                Math.abs(c.getLocationX() - a.getAction().getLocationX()) <= visionRadius &&
                                Math.abs(c.getLocationY() - a.getAction().getLocationY()) <= visionRadius
                        )
                )
                .map(attackMapper::toKnownCellResponse)
                .toList();
    }

    public List<Attack> findAttacksByPlayerIdAndTurn(Long playerId, int turn) {
        return attackRepository.findByAction_Actor_IdAndAction_CreatedAtTurn(playerId, turn);
    }

    public List<Attack> findAttacksByTurn(int turn) {
        return attackRepository.findByAction_CreatedAtTurn(turn);
    }

    public List<AttackHit> findAttackHitsByObjectOwnerIdAndTurn(Long playerId, int turn) {
        return attackHitRepository.findByObjectOwner_IdAndAttack_Action_CreatedAtTurn(
                playerId,
                turn
        );
    }

    public List<AttackHit> findAttackHitsByTurn(int turn) {
        return attackHitRepository.findByAttack_Action_CreatedAtTurn(turn);
    }
}
