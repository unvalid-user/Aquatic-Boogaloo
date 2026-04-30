package com.example.aquaticboogaloo.mapper;

import com.example.aquaticboogaloo.dto.mapper.field_object.FieldObjectMapper;
import com.example.aquaticboogaloo.dto.mapper.field_object.FieldObjectMapperImpl;
import com.example.aquaticboogaloo.dto.response.field.MineResponse;
import com.example.aquaticboogaloo.dto.response.field.ScanResponse;
import com.example.aquaticboogaloo.dto.response.field.ShipResponse;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.entity.enums.ActionStatus;
import com.example.aquaticboogaloo.entity.enums.ActionType;
import com.example.aquaticboogaloo.entity.field_objects.Mine;
import com.example.aquaticboogaloo.entity.field_objects.Scan;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static com.example.aquaticboogaloo.util.EntityBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;


public class FieldObjectMapperTest {
    private final FieldObjectMapper mapper = new FieldObjectMapperImpl();

    private static final long randomLongBound = 100000;
    private final Random random = new Random();
    private Long randomLong() { return random.nextLong(randomLongBound); }

    @Test
    void shipToShipResponse() {
        Ship ship = buildShip();
        ShipResponse response = mapper.toResponse(ship);

        assertThat(response.getId()).isEqualTo(ship.getId());
        assertThat(response.getPlayerId()).isEqualTo(ship.getOwner().getId());
        assertThat(response.getShipCells().size()).isEqualTo(2);
        assertThat(response.getShipCells().get(0).getId()).isEqualTo(ship.getShipCells().get(0).getId());
    }

    @Test
    void mineToMineResponse() {
        Mine mine = buildMine();
        MineResponse response = mapper.toResponse(mine);

        assertThat(response.getObjectId()).isEqualTo(mine.getId());
        assertThat(response.getPlayerId()).isEqualTo(mine.getAction().getActor().getId());
    }

    @Test
    void scanToScanResponse() {
        Scan scan = buildScan();
        ScanResponse response = mapper.toResponse(scan);

        assertThat(response.getObjectId()).isEqualTo(scan.getId());
        assertThat(response.getPlayerId()).isEqualTo(scan.getAction().getActor().getId());
    }

    private Scan buildScan() {
        Scan scan = new Scan();
        scan.setId(randomLong());
        scan.setMinesNumber(5);
        scan.setShipCellsNumber(3);
        scan.setExpirationTurn(6);
        scan.setAction(action(ActionType.SCAN, 5, 5, 5));
        buildPlayerWithUser().addAction(scan.getAction());
        scan.getAction().setId(randomLong());
        scan.getAction().setStatus(ActionStatus.COMPLETED);

        return scan;
    }


    private Ship buildShip() {
        Ship ship = shipWithTwoCells(5, 6);
        ship.setId(randomLong());
        ship.getShipCells().forEach(sc -> sc.setId(randomLong()));

        buildPlayerWithUser().addShip(ship);

        return ship;
    }

    private Mine buildMine() {
        int turnNumber = 5;
        Mine mine = mine(4, 6, turnNumber);
        mine.setId(randomLong());
        mine.getAction().setId(randomLong());

        buildPlayerWithUser().addAction(mine.getAction());

        return mine;
    }

    private Player buildPlayerWithUser() {
        User user = new User(UUID.randomUUID().toString());
        user.setId(randomLong());

        Player player = player(user);
        player.setId(randomLong());

        return player;
    }
}
