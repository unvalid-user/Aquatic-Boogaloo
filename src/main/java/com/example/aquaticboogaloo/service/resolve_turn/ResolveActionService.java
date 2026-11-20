package com.example.aquaticboogaloo.service.resolve_turn;

import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.AttackHit;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.enums.*;
import com.example.aquaticboogaloo.entity.field_objects.*;
import com.example.aquaticboogaloo.repository.*;
import com.example.aquaticboogaloo.util.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.aquaticboogaloo.service.resolve_turn.ActionFailMessages.*;

@Service
@RequiredArgsConstructor
public class ResolveActionService {

    private final ActionRepository actionRepository;
    private final ShieldRepository shieldRepository;
    private final ShipCellRepository shipCellRepository;
    private final MineRepository mineRepository;
    private final AttackHitRepository attackHitRepository;
    private final AttackRepository attackRepository;
    private final ScanRepository scanRepository;


    @Transactional
    public void resolveScanActions(Game game) {
        List<Action> scanActions = getActionsForResolving(game, ActionType.PLACE_SCAN);

        List<Scan> scans = new ArrayList<>();
        scanActions.forEach(action -> {
            Scan scan = new Scan();
            scan.setGame(game);
            scan.setAction(action);
            scan.setExpirationTurn(game.getCurrentTurn() + 1);
            scan.setShipCellsNumber(getShipCellsNumberInRadius(
                    game,
                    action.getLocationX(),
                    action.getLocationY(),
                    game.getRuleset().getScanRadius()
            ));
            scan.setMinesNumber(getMinesNumberInRadius(
                    game,
                    action.getLocationX(),
                    action.getLocationY(),
                    game.getRuleset().getScanRadius()
            ));

            scans.add(scan);

            action.setStatus(ActionStatus.COMPLETED);
        });

        scanRepository.saveAll(scans);
    }

    @Transactional
    public void resolveShieldActions(Game game) {
        List<Action> shieldActions = getActionsForResolving(game, ActionType.PLACE_SHIELD);

        List<Shield> shields = new ArrayList<>();
        shieldActions.forEach(action -> {
            Shield shield = new Shield();
            shield.setGame(game);
            shield.setAction(action);
            shield.setExpirationTurn(game.getCurrentTurn());

            shields.add(shield);

            action.setStatus(ActionStatus.COMPLETED);
        });
        shieldRepository.saveAll(shields);
    }

    // All stages share the turn transaction and its game lock.
    @Transactional
    public void resolveMineActions(Game game) {
        List<Action> mineActions = getActionsForResolving(game, ActionType.PLACE_MINE);

        // group all planned mines by coords
        var groupedByCoords = mineActions.stream()
                .collect(Collectors.groupingBy(a -> new Point(a.getLocationX(), a.getLocationY())));

        List<Mine> mines = new ArrayList<>();
        groupedByCoords.forEach((p, actions) -> {
            if (actions.size() > 1) {
                actions.forEach(a ->
                        a.setFail(MINES_CONFLICT));
                return;
            }

            Action action = actions.getFirst();
            if (isCellBlockedByShield(game, p.x(), p.y())) {
                action.setFail(SHIELD_BLOCK);
            } else if (isCellBlockedByShip(game, p.x(), p.y())) {
                action.setFail(SHIP_BLOCK);
            } else if (isCellBlockedByMine(game, p.x(), p.y())) {
                action.setFail(MINE_BLOCK);
            } else {
                Mine mine = new Mine();
                mine.setGame(game);
                mine.setAction(action);
                mines.add(mine);

                action.setStatus(ActionStatus.COMPLETED);
            }
        });

        mineRepository.saveAll(mines);
    }

    /*
    if shield has been hit - shields' owners* must receive notifications
    *there are could be several shields in one cell

    even if shield is used, it can absorb next attacks in this turn
    used shields should be deleted at the end of this function


    the same situation with mines: notification + deletion at the end
    + damage to the random attacker's ship


    if ship have been hit - ALL attackers get bonus
    shipCell destroys at the end of the function
     */
    @Transactional
    public void resolveAttackActions(Game game) {
        List<Action> attackActions = getActionsForResolving(game, ActionType.ATTACK);

        var groupedByCoords = attackActions.stream()
                .collect(Collectors.groupingBy(a -> new Point(a.getLocationX(), a.getLocationY())));

        Map<Ship, Set<AttackHit>> shipAttackHits = new HashMap<>();
        List<AttackHit> allAttackHits = new ArrayList<>();
        Map<Mine, List<Action>> mineAttacks = new HashMap<>();
        // Resolve direct shots before mine retaliation, with a stable coordinate order.
        // A mine cannot turn another player's simultaneous direct hit into a miss.
        groupedByCoords.forEach((point, actions) -> {
            // resolving shield hits
            var shields = getShieldsByCoordinate(game, point.x(), point.y());
            if (!shields.isEmpty()) {
                resolveShieldAttackHits(actions, shields);
                return;
            }

            // resolving shipCell hits
            var shipCell = getShipCellByCoordinate(game, point.x(), point.y());
            if (shipCell.isPresent() && !shipCell.get().isDestroyed()) {
                var attackHits = resolveShipAttackHits(actions, shipCell.get());
                allAttackHits.addAll(attackHits);
                shipAttackHits
                    .computeIfAbsent(shipCell.get().getShip(), ship -> new HashSet<>())
                    .addAll(attackHits);

                return;
            }

            // collection mine hits and resolve them later
            var mine = getMineByCoordinate(game, point.x(), point.y());
            if (mine.isPresent()) {
                mineAttacks.put(mine.get(), actions);
                return;
            }


            List<Attack> attacks = new ArrayList<>();
            actions.forEach(action -> {
                Attack attack = new Attack();
                attack.setAction(action);
                attack.setStatus(AttackStatus.MISS);

                attacks.add(attack);

                action.setStatus(ActionStatus.COMPLETED);
            });
            attackRepository.saveAll(attacks);
        });

        // resolving mine hits
        mineAttacks.forEach((mine, attacks) -> {
            var mineHits = resolveMineAttackHits(attacks, mine);

            allAttackHits.addAll(mineHits);
            mineHits.stream()
                    .filter(attackHit -> attackHit.getMineHitBack() != null)
                    .collect(Collectors.groupingBy(attackHit -> attackHit.getMineHitBack().getShip()))
                    .forEach((ship, hits) -> {
                        shipAttackHits
                                .computeIfAbsent(ship, ship_ -> new HashSet<>())
                                .addAll(hits);
                    });
        });

        shipAttackHits.forEach((ship, hits) -> {
            if (!changeShipStatusToDestroyed(ship)) return;

            var shipHits = hits.stream()
                    .filter(hit -> hit.getMineHitBack() == null)
                    .toList();

            shipHits.forEach(hit -> {
                hit.setHitImpact(AttackHitImpact.DESTROYED);
            });
            shipHits.stream()
                    .map(hit -> hit.getAttack().getAction().getActor())
                    .distinct()
                    .forEach(player ->
                            player.addPoints(game.getRuleset().getShipDestroyBonus())
                    );
        });

        attackHitRepository.saveAll(allAttackHits);
    }

    private boolean changeShipStatusToDestroyed(Ship ship) {
        boolean shipNotDestroyed = ship.getShipCells().stream()
                .anyMatch(shipCell -> !shipCell.isDestroyed());
        if (shipNotDestroyed) return false;

        ship.setStatus(ShipStatus.DESTROYED);
        return true;
    }

    /*
    create and save Attack + AttackHits
    mine penalty
    set ShipCell.isDestroyed = true
    set Ship.status = DAMAGED
    delete mine
     */
    private List<AttackHit> resolveMineAttackHits(List<Action> actions, Mine mine) {
        List<AttackHit> attackHits = new ArrayList<>();
        actions.forEach(action -> {
            Attack attack = new Attack();
            attack.setAction(action);
            attack.setStatus(AttackStatus.HIT);

            AttackHit attackHit = new AttackHit();
            attackHit.setAttack(attack);
            attack.setHit(attackHit);
            attackHit.setObjectType(FieldObjectType.MINE);
            attackHit.setObjectOwner(mine.getAction().getActor());
            attackHit.setHitImpact(AttackHitImpact.DESTROYED);

            attackHits.add(attackHit);

            // damage back
            var shipCells = getIntactShipCellsByPlayer(action.getActor());
            // damage attacker's ship IF player is ALIVE
            if (!shipCells.isEmpty()) {
                ShipCell randomShipCell = shipCells.get(new Random().nextInt(shipCells.size()));

                attackHit.setMineHitBack(randomShipCell);

                randomShipCell.setDestroyed(true);
                randomShipCell.getShip().setStatus(ShipStatus.DAMAGED);
            }

            mine.getAction().getActor()
                    .addPoints(action.getActor().getGame().getRuleset().getMineHitBackBonus());
            action.getActor()
                    .removePoints(action.getActor().getGame().getRuleset().getMineHitBackPenalty());

            action.setStatus(ActionStatus.COMPLETED);
        });

        mineRepository.delete(mine);

        return attackHits;
    }

    /*
    create and RETURN (not save yet) Attack + AttackHits
    set ShipCell.isDestroyed = true
    set Ship.status = DAMAGED
     */
    private List<AttackHit> resolveShipAttackHits(List<Action> actions, ShipCell shipCell) {
        List<AttackHit> attackHits = new ArrayList<>();
        actions.forEach(action -> {
            Attack attack = new Attack();
            attack.setAction(action);
            attack.setStatus(AttackStatus.HIT);

            AttackHit attackHit = new AttackHit();
            attackHit.setAttack(attack);
            attack.setHit(attackHit);
            attackHit.setObjectType(FieldObjectType.SHIP);
            attackHit.setHitShipCell(shipCell);
            attackHit.setObjectOwner(shipCell.getShip().getOwner());
            attackHit.setHitImpact(AttackHitImpact.DAMAGED);

            attackHits.add(attackHit);

            action.getActor().addPoints(action.getActor().getGame().getRuleset().getShipHitBonus());

            action.setStatus(ActionStatus.COMPLETED);
        });

        shipCell.getShip().getOwner()
                // TODO: to many entity calls
                .removePoints(shipCell.getShip().getOwner().getGame().getRuleset().getShipHitPenalty());
        shipCell.setDestroyed(true);
        shipCell.getShip().setStatus(ShipStatus.DAMAGED);

        return attackHits;
    }

    // Every shield owner is notified, even when several shields occupy the same cell.
    private void resolveShieldAttackHits(List<Action> actions, List<Shield> shields) {
        List<AttackHit> attackHits = new ArrayList<>();
        actions.forEach(action -> {
            Attack attack = new Attack();
            attack.setAction(action);
            attack.setStatus(AttackStatus.HIT);

            // TODO: ???
            Shield shield = shields.getFirst();
            AttackHit attackHit = new AttackHit();
            attackHit.setAttack(attack);
            attack.setHit(attackHit);
            attackHit.setObjectType(FieldObjectType.SHIELD);
            attackHit.setObjectOwner(shield.getAction().getActor());
            attackHit.setHitImpact(AttackHitImpact.DESTROYED);
            attackHits.add(attackHit);

            action.setStatus(ActionStatus.COMPLETED);
        });

        attackHitRepository.saveAll(attackHits);
    }

    private List<ShipCell> getIntactShipCellsByPlayer(Player player) {
        return player.getShips().stream()
                .flatMap(ship -> ship.getShipCells().stream())
                .filter(sc -> !sc.isDestroyed())
                .toList();
    }


    public int getShipCellsNumberInRadius(Game game, int x, int y, int r) {
        return shipCellRepository.getNumberOfShipCellsInRadius(game.getId(), x, y, r);
    }
    public int getMinesNumberInRadius(Game game, int x, int y, int r) {
        return mineRepository.getNumberOfMinesInRadius(game.getId(), x, y, r);
    }

    public List<Shield> getShieldsByCoordinate(Game game, int x, int y) {
        return shieldRepository.findByCoordinate(game.getId(), x, y);
    }
    public Optional<ShipCell> getShipCellByCoordinate(Game game, int x, int y) {
        return shipCellRepository.findByCoordinate(game.getId(), x, y);
    }
    public Optional<Mine> getMineByCoordinate(Game game, int x, int y) {
        return mineRepository.findByCoordinate(game.getId(), x, y);
    }

    public boolean isCellBlockedByShip(Game game, int x, int y) {
        return shipCellRepository.existsByCoordinate(game.getId(), x, y);
    }
    public boolean isCellBlockedByMine(Game game, int x, int y) {
        return mineRepository.existsByCoordinate(game.getId(), x, y);
    }
    public boolean isCellBlockedByShield(Game game, int x, int y) {
        return shieldRepository.existsByCoordinate(game.getId(), x, y);
    }

    private List<Action> getActionsForResolving(Game game, ActionType actionType) {
        return actionRepository.findByTypeAndStatus(
                game.getId(),
                game.getCurrentTurn(),
                ActionStatus.PLANNED,
                actionType
        );
    }
}
