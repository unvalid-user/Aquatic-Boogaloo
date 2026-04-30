package com.example.aquaticboogaloo;

import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.GameRuleset;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.service.GameFieldInitializer;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameFieldInitializerTest {

    @Test
    void testGameFieldInitializer() {
        Game game = createGameWithPlayers();

        GameFieldInitializer initializer = new GameFieldInitializer(game);
        initializer.initializeAndBuildEntities();

        var rs = game.getRuleset();
        final int shipsQuantity = rs.getK1ShipsQuantity()
                + rs.getK2ShipsQuantity()
                + rs.getK3ShipsQuantity()
                + rs.getK4ShipsQuantity();


        game.getPlayers().forEach(player -> {
            int[] nk = new int[]{0,0,0,0};
            player.getShips().forEach(ship -> {
                nk[ship.getType().getLength()-1]++;
            });
            assertEquals(player.getShips().size(), shipsQuantity);
            assertEquals(nk[0], rs.getK1ShipsQuantity());
            assertEquals(nk[1], rs.getK2ShipsQuantity());
            assertEquals(nk[2], rs.getK3ShipsQuantity());
            assertEquals(nk[3], rs.getK4ShipsQuantity());
        });
    }


    Game createGameWithPlayers() {
        GameRuleset ruleset = new GameRuleset();
        Game game = new Game();
        game.setId(10L);
        game.setRuleset(ruleset);
        List<Player> players = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            Player p = new Player();
            p.setId( (long)i );
            p.setGame(game);
            players.add(p);
        }
        game.setPlayers(players);

        return game;
    }
}
