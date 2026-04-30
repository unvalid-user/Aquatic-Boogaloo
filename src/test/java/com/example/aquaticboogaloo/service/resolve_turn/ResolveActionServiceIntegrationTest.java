package com.example.aquaticboogaloo.service.resolve_turn;

import com.example.aquaticboogaloo.entity.Action;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.GameRuleset;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.enums.AttackHitImpact;
import com.example.aquaticboogaloo.entity.enums.AttackStatus;
import com.example.aquaticboogaloo.entity.enums.FieldObjectType;
import com.example.aquaticboogaloo.entity.enums.GameStatus;
import com.example.aquaticboogaloo.entity.enums.PlayerStatus;
import com.example.aquaticboogaloo.entity.enums.ShipStatus;
import com.example.aquaticboogaloo.entity.enums.ShipType;
import com.example.aquaticboogaloo.entity.field_objects.*;
import com.example.aquaticboogaloo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Comparator;
import java.util.UUID;

import static com.example.aquaticboogaloo.service.resolve_turn.ActionFailMessages.MINES_CONFLICT;
import static com.example.aquaticboogaloo.util.EntityBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.datasource.hikari.maximum-pool-size=2",
        "spring.datasource.hikari.minimum-idle=0",
        "spring.datasource.hikari.max-lifetime=10000",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ResolveActionServiceIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private ResolveActionService resolveActionService;
    @Autowired private ActionRepository actionRepository;
    @Autowired private AttackRepository attackRepository;
    @Autowired private AttackHitRepository attackHitRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private MineRepository mineRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private ShipCellRepository shipCellRepository;
    @Autowired private ShipRepository shipRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ShieldRepository shieldRepository;
    @Autowired private ScanRepository scanRepository;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE users, game_rulesets RESTART IDENTITY CASCADE");
    }

    @Test
    void resolveMineActions_shouldFailAllMinesInSameCell() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.PLACE_MINE, 4, 5, context.game.getCurrentTurn()));
        context.defender.addAction(action(ActionType.PLACE_MINE, 4, 5, context.game.getCurrentTurn()));
        gameRepository.saveAndFlush(context.game);

        resolveActionService.resolveMineActions(context.game);

        assertThat(mineRepository.findAll()).isEmpty();
        assertThat(actionRepository.findAll())
                .hasSize(2)
                .allSatisfy(action -> {
                    assertThat(action.getStatus()).isEqualTo(ActionStatus.FAILED);
                    assertThat(action.getFailCauseMessage()).isEqualTo(MINES_CONFLICT);
                });
    }

    @Test
    void resolveMineActions_shouldPlaceMinesSuccessfully() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.PLACE_MINE, 4, 5, context.game.getCurrentTurn()));
        context.defender.addAction(action(ActionType.PLACE_MINE, 4, 6, context.game.getCurrentTurn()));
        gameRepository.saveAndFlush(context.game);

        resolveActionService.resolveMineActions(context.game);

        assertThat(mineRepository.findAll()).hasSize(2);
        assertThat(actionRepository.findAll())
                .hasSize(2)
                .allSatisfy(action -> {
                    assertThat(action.getStatus()).isEqualTo(ActionStatus.COMPLETED);
                });
    }

    @Test
    void resolveShieldAction_shouldPlaceShieldsSuccessfully() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.PLACE_SHIELD, 4, 5, context.game.getCurrentTurn()));
        context.defender.addAction(action(ActionType.PLACE_SHIELD, 4, 5, context.game.getCurrentTurn()));
        gameRepository.saveAndFlush(context.game);

        resolveActionService.resolveShieldActions(context.game);

        assertThat(shieldRepository.findAll()).hasSize(2);
        assertThat(actionRepository.findAll())
                .hasSize(2)
                .allSatisfy(action -> {
                    assertThat(action.getStatus()).isEqualTo(ActionStatus.COMPLETED);
                });
    }

    @Test
    void resolveScanAction_shouldPlaceScanSuccessfully() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.SCAN, 4, 5, context.game.getCurrentTurn()));
        context.defender.addShip(shipWithTwoCells(4, 5));
        Mine mine = mine(5, 6, context.game.getCurrentTurn()-1);
        mine.setGame(context.game);
        context.defender.addAction(mine.getAction());
        gameRepository.saveAndFlush(context.game);
        mineRepository.saveAndFlush(mine);

        resolveActionService.resolveScanActions(context.game);

        var scans = scanRepository.findAll();
        assertThat(scans).hasSize(1);

        assertThat(scans.getFirst())
                .hasFieldOrPropertyWithValue(Scan_.MINES_NUMBER, 1)
                .hasFieldOrPropertyWithValue(Scan_.SHIP_CELLS_NUMBER, 2)
                .hasFieldOrPropertyWithValue(Scan_.EXPIRATION_TURN, context.game.getCurrentTurn()+1);

        assertThat(actionRepository.findAll())
                .hasSize(2)
                .allSatisfy(action -> {
                    assertThat(action.getStatus()).isEqualTo(ActionStatus.COMPLETED);
                });
    }

    @Test
    void resolveAttackActions_shouldPersistShipHitAndDestroyTargetCell() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.ATTACK, 6, 7, context.game.getCurrentTurn()));
        context.defender.addShip(shipWithOneCell(6, 7));
        gameRepository.saveAndFlush(context.game);

        resolveActionService.resolveAttackActions(context.game);

        var resolvedAction = actionRepository.findAll().getFirst();
        assertThat(resolvedAction.getStatus()).isEqualTo(ActionStatus.COMPLETED);

        var attacks = attackRepository.findAll();
        assertThat(attacks).hasSize(1);
        var attack = attacks.getFirst();
        assertThat(attack.getStatus()).isEqualTo(AttackStatus.SHIP_HIT);

        var hits = attackHitRepository.findAll();
        assertThat(hits).hasSize(1);
        var hit = hits.getFirst();
        assertThat(hit.getObjectType()).isEqualTo(FieldObjectType.SHIP);
        assertThat(hit.getHitImpact()).isEqualTo(AttackHitImpact.DESTROYED);

        var destroyedCell = shipCellRepository.findAll().getFirst();
        assertThat(destroyedCell.isDestroyed()).isTrue();
        assertThat(shipRepository.findAll().getFirst().getStatus()).isEqualTo(ShipStatus.DESTROYED);

        var players = playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getId))
                .toList();
        assertThat(players.get(0).getPoints()).isEqualTo(2);
        assertThat(players.get(1).getPoints()).isEqualTo(-1);
    }

    @Test
    void resolveAttackActions_shouldHitMineAndAttackBack() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.ATTACK, 6, 7, context.game.getCurrentTurn()));
        context.attacker.addShip(shipWithTwoCells(2, 2));
        Mine mine = mine(6, 7, context.game.getCurrentTurn()-1);
        mine.setGame(context.game);
        context.defender.addAction(mine.getAction());
        gameRepository.saveAndFlush(context.game);
        mineRepository.saveAndFlush(mine);

        resolveActionService.resolveAttackActions(context.game);

        var resolvedAction = actionRepository.findAll().stream()
                .filter(action -> action.getType() == ActionType.ATTACK)
                .findFirst()
                .orElseThrow();
        assertThat(resolvedAction.getStatus()).isEqualTo(ActionStatus.COMPLETED);

        var attacks = attackRepository.findAll();
        assertThat(attacks).hasSize(1);
        var attack = attacks.getFirst();
        assertThat(attack.getStatus()).isEqualTo(AttackStatus.MINE_HIT);

        var hits = attackHitRepository.findAll();
        assertThat(hits).hasSize(1);
        var hit = hits.getFirst();
        assertThat(hit.getObjectType()).isEqualTo(FieldObjectType.MINE);
        assertThat(hit.getHitImpact()).isEqualTo(AttackHitImpact.DESTROYED);

        var destroyedCells = shipCellRepository.findAll().stream()
                .filter(ShipCell::isDestroyed)
                .toList();
        assertThat(destroyedCells.size()).isEqualTo(1);
        assertThat(shipRepository.findAll().getFirst().getStatus()).isEqualTo(ShipStatus.DAMAGED);

        var players = playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getId))
                .toList();
        assertThat(players.get(0).getPoints()).isEqualTo(-3);   // attacker
        assertThat(players.get(1).getPoints()).isEqualTo(2);    // defender
    }

    @Test
    void resolveAttackActions_shieldShouldBlockAttacks() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.ATTACK, 2, 2, context.game.getCurrentTurn()));
        context.defender.addShip(shipWithOneCell(2, 2));
        Shield shield = shield(2, 2, context.game.getCurrentTurn());
        context.defender.addAction(shield.getAction());
        shield.setGame(context.game);
        gameRepository.saveAndFlush(context.game);
        shieldRepository.saveAndFlush(shield);

        resolveActionService.resolveAttackActions(context.game);

        actionRepository.findAll().forEach(action -> {
            assertThat(action.getStatus()).isEqualTo(ActionStatus.COMPLETED);
        });


        var attacks = attackRepository.findAll();
        assertThat(attacks).hasSize(1);
        var attack = attacks.getFirst();
        assertThat(attack.getStatus()).isEqualTo(AttackStatus.BLOCKED);

        var hits = attackHitRepository.findAll();
        assertThat(hits).hasSize(1);
        var hit = hits.getFirst();
        assertThat(hit.getObjectType()).isEqualTo(FieldObjectType.SHIELD);
        assertThat(hit.getHitImpact()).isEqualTo(AttackHitImpact.DESTROYED);

        var shipCells = shipCellRepository.findAll().stream()
                .filter(ShipCell::isDestroyed)
                .toList();
        assertThat(shipCells.size()).isEqualTo(0);
        assertThat(shipRepository.findAll().getFirst().getStatus()).isEqualTo(ShipStatus.INTACT);

        var players = playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getId))
                .toList();
        assertThat(players.get(0).getPoints()).isEqualTo(0);
        assertThat(players.get(1).getPoints()).isEqualTo(0);
    }

    @Test
    void resolveAttackActions_shouldDamageShipWithSeveralCells() {
        var context = createGame();
        context.attacker.addAction(action(ActionType.ATTACK, 6, 7, context.game.getCurrentTurn()));
        context.defender.addShip(shipWithTwoCells(6, 7));
        Long gameId = gameRepository.saveAndFlush(context.game).getId();

        resolveActionService.resolveAttackActions(context.game);

        gameRepository.findById(gameId);

        var resolvedAction = actionRepository.findAll().getFirst();
        assertThat(resolvedAction.getStatus()).isEqualTo(ActionStatus.COMPLETED);

        var attacks = attackRepository.findAll();
        assertThat(attacks).hasSize(1);
        var attack = attacks.getFirst();
        assertThat(attack.getStatus()).isEqualTo(AttackStatus.SHIP_HIT);

        var hits = attackHitRepository.findAll();
        assertThat(hits).hasSize(1);
        var hit = hits.getFirst();
        assertThat(hit.getObjectType()).isEqualTo(FieldObjectType.SHIP);
        assertThat(hit.getHitImpact()).isEqualTo(AttackHitImpact.DAMAGED);

        var destroyedCells = shipCellRepository.findAll().stream()
                        .sorted(Comparator.comparingInt(ShipCell::getLocationX)
                                .thenComparingInt(ShipCell::getLocationY))
                        .toList();
        assertThat(destroyedCells.get(0).isDestroyed()).isFalse();
        assertThat(destroyedCells.get(1).isDestroyed()).isTrue();
        assertThat(shipRepository.findAll().getFirst().getStatus()).isEqualTo(ShipStatus.DAMAGED);

        var players = playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getId))
                .toList();
        assertThat(players.get(0).getPoints()).isEqualTo(1);
        assertThat(players.get(1).getPoints()).isEqualTo(-1);
    }


    private TestGameContext createGame() {
        User host = userRepository.save(new User("host-" + UUID.randomUUID()));
        User attackerUser = userRepository.save(new User("attacker-" + UUID.randomUUID()));
        User defenderUser = userRepository.save(new User("defender-" + UUID.randomUUID()));

        GameRuleset ruleset = new GameRuleset();
        ruleset.setShipHitBonus(1);
        ruleset.setShipHitPenalty(1);
        ruleset.setShipDestroyBonus(1);

        ruleset.setMineHitBackBonus(2);
        ruleset.setMineHitBackPenalty(3);

        Game game = new Game();
        game.setTitle("test game");
        game.setHostUser(host);
        game.setRuleset(ruleset);
        game.setStatus(GameStatus.ACTIVE);
        game.setFieldWidth(10);
        game.setFieldHeight(10);
        game.setCurrentTurn(2);
        game.setRemainTurns(10);

        Player attacker = player(attackerUser);
        Player defender = player(defenderUser);
        game.addPlayer(attacker);
        game.addPlayer(defender);

        return new TestGameContext(game, attacker, defender);
    }



    private record TestGameContext(Game game, Player attacker, Player defender) {
    }
}
