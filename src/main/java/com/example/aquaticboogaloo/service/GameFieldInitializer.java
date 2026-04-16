package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.controller.game_field_view.GameFieldView;
import com.example.aquaticboogaloo.controller.game_field_view.ShipView;
import com.example.aquaticboogaloo.entity.Game;
import com.example.aquaticboogaloo.entity.Player;
import com.example.aquaticboogaloo.entity.field_objects.Ship;
import com.example.aquaticboogaloo.entity.field_objects.ShipCell;
import com.example.aquaticboogaloo.entity.enums.ShipType;
import com.example.aquaticboogaloo.util.Point;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.ToIntFunction;

// TODO: concurrency + optimizations

public class GameFieldInitializer {
    private final List<Area> areas = new ArrayList<>();
    private final Random random = new Random();
    private final int[] Nk;
    private final int[] SHIP_QUANTITY;
    private final List<Integer> nkWeighted = new ArrayList<>(10);

    private int iterX = 0;
    private int iterY = 0;
    private int iterRadius = 0;
    private Direction iterDirection = Direction.RIGHT;

    private final Game game;

    public GameFieldInitializer(Game game_) {
        game = game_;

        final int SHIP_TYPES_COUNT = 4;
        SHIP_QUANTITY = new int[SHIP_TYPES_COUNT];
        SHIP_QUANTITY[0] = game.getRuleset().getK1ShipsQuantity();
        SHIP_QUANTITY[1] = game.getRuleset().getK2ShipsQuantity();
        SHIP_QUANTITY[2] = game.getRuleset().getK3ShipsQuantity();
        SHIP_QUANTITY[3] = game.getRuleset().getK4ShipsQuantity();

        Nk = new int[SHIP_TYPES_COUNT];
        for (int i = 0; i < Nk.length; i++) {
            Nk[i] = SHIP_QUANTITY[i];
            for (int j = 0; j < Nk[i]; j++) {
                nkWeighted.add(i);
            }
            Nk[i] *= game.getPlayers().size();
        }
    }

    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    private static class Area {
        private Point min;
        private Point max;

        public Area(int x1, int y1, int x2, int y2) {
            min = new Point(
                    Integer.min(x1, x2),
                    Integer.min(y1, y2)
            );
            max = new Point(
                    Integer.max(x1, x2),
                    Integer.max(y1, y2)
            );
        }

        public void addOffset(int offsetX, int offsetY) {
            min = new Point(min.x() + offsetX, min.y() + offsetY);
            max = new Point(max.x() + offsetX, max.y() + offsetY);
        }

        public static Area createShipArea(int x, int y, Direction direction, int shipLength) {
            Area a = new Area();
            int minX = direction == Direction.LEFT ? x-shipLength : x-1;
            int minY = direction == Direction.UP ? y-shipLength : y-1;
            int maxX = direction == Direction.RIGHT ? x+shipLength : x+1;
            int maxY = direction == Direction.DOWN ? y+shipLength : y+1;

            a.min = new Point(minX, minY);
            a.max = new Point(maxX, maxY);

            return a;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj instanceof Area a) {
                return min.equals(a.min) && max.equals(a.max)
                        || max.equals(a.min) && min.equals(a.max);
            }
            return false;
        }
    }

    public void initializeAndBuildEntities() {
        initializeShipsAreas();
        offsetAreasToZeros();
        buildEntities();
    }

    private void initializeShipsAreas() {
        final int RANDOM_RADIUS = 2;

        while (!isNkEmpty()) {
            moveToNextFreeCell();
            Direction direction = pickDirection();

            // initialize cords
            int x = iterX;
            int y = iterY;
            int t = random.nextInt(RANDOM_RADIUS * RANDOM_RADIUS);
            x += (t / RANDOM_RADIUS) * (direction == Direction.UP || direction == Direction.LEFT ? -1 : 1);
            y += (t % RANDOM_RADIUS) * (direction == Direction.UP || direction == Direction.RIGHT ? -1 : 1);

            int maxShipLength = findShipMaxLength(x, y, direction);
            if (maxShipLength < 1) continue;

            int shipLength = pickRandomShipLength(maxShipLength);
            if (shipLength < 1) continue;

            areas.add(Area.createShipArea(x, y, direction, shipLength));
        }
    }

    private void offsetAreasToZeros() {
        int offsetX = -findFieldMinX() -1;
        int offsetY = -findFieldMinY() -1;

        areas.forEach(a -> a.addOffset(offsetX, offsetY));
    }

    // TODO: not sure about -1
    public int getFieldWidth() {
        return findFieldMaxX() - findFieldMinX() -1;
    }
    public int getFieldHeight() {
        return findFieldMaxY() - findFieldMinY() -1;
    }

    private void buildEntities() {
        List<Ship> ships = buildShipsFromAreas();

        // TODO: fix uneven distribution
        game.getPlayers().forEach(player -> {
            System.arraycopy(SHIP_QUANTITY, 0, Nk, 0, Nk.length);

            ships.stream()
                    .filter(ship -> ship.getOwner() == null)
                    .forEach(ship -> {
                        int i = ship.getType().getLength()-1;
                        if (Nk[i] > 0) {
                            Nk[i]--;
                            player.addShip(ship);
                        }
                    });
        });

        game.setFieldWidth(getFieldWidth());
        game.setFieldHeight(getFieldHeight());
    }

    private List<Ship> buildShipsFromAreas() {
        return areas.stream().map(area -> {
            Ship ship = new Ship();

            for (int x = area.min.x() + 1; x < area.max.x(); x++) {
                for (int y = area.min.y() + 1; y < area.max.y(); y++) {
                    ShipCell shipCell = new ShipCell();
                    shipCell.setLocationX(x);
                    shipCell.setLocationY(y);

                    ship.addCell(shipCell);
                }
            }
            ship.setType(ShipType.fromLength(ship.getShipCells().size()));
            return ship;
        }).toList();
    }

    private GameFieldView buildResponse() {
        List<ShipView> ships = new LinkedList<>();

        areas.forEach(area -> {
            int x = area.min.x() + 1;
            int y = area.min.y() + 1;
            String orientation = "H";

            int hLength = area.max.x() - area.min.x() -1;
            int vLength = area.max.y() - area.min.y() -1;
            int length = hLength;

            if (vLength > hLength) {
                orientation = "V";
                length = vLength;
            }

            ships.add(new ShipView(
                    length,
                    x,
                    y,
                    orientation
            ));
        });

        return new GameFieldView(
                findFieldMaxX(),
                findFieldMaxY(),
                ships
        );
    }

    private int findShipMaxLength(int x, int y, Direction direction) {
        int n = 0;
        while (!isCellBlocked(x, y) && n < 4) {
            switch (direction) {
                case DOWN -> y++;
                case LEFT -> x--;
                case UP -> y--;
                case RIGHT -> x++;
            }
            n++;
        }

        return n;
    }

    private int moveToNextFreeCell() {
        int i = 0;
        do {
            iterateNext();
            i++;
        } while (isCurrentCellBlocked());
        return i;
    }

    private void iterateNext() {
        switch (iterDirection) {
            case DOWN -> {
                iterY++;
                if (iterY == iterRadius) iterDirection = Direction.LEFT;
            }
            case LEFT -> {
                iterX--;
                if (iterX == -iterRadius) iterDirection = Direction.UP;
            }
            case UP -> {
                iterY--;
                if (iterY == -iterRadius) iterDirection = Direction.RIGHT;
            }
            case RIGHT -> {
                iterX++;
                if (iterX == iterRadius + 1) {
                    iterDirection = Direction.DOWN;
                    iterRadius++;
                }
            }
        }
    }


    private boolean isCurrentCellBlocked() { return isCellBlocked(iterX, iterY); }
    private boolean isCellBlocked(int x, int y) {
        // not sure about inclusive/exclusive
        return areas.stream()
                .anyMatch(a ->
                        isIntInRange(x, a.min.x(), a.max.x()) &&
                        isIntInRange(y, a.min.y(), a.max.y())
                );
    }

    private int findFieldMinX() { return findFieldMinCoordinate(a -> a.min.x()); }
    private int findFieldMinY() { return findFieldMinCoordinate(a -> a.min.y()); }
    private int findFieldMaxX() { return findFieldMaxCoordinate(a -> a.max.x()); }
    private int findFieldMaxY() { return findFieldMaxCoordinate(a -> a.max.y()); }
    private int findFieldMinCoordinate(ToIntFunction<Area> mapper) {
        return areas.stream()
                .mapToInt(mapper)
                .min()
                .orElse(0);
    }
    private int findFieldMaxCoordinate(ToIntFunction<Area> mapper) {
        return areas.stream()
                .mapToInt(mapper)
                .max()
                .orElse(0);
    }

    private Direction pickDirection() {
        // I could pick Horizontal or Vertical placement instead of this
        Direction secondDirection;
        switch (iterDirection) {
            case DOWN -> secondDirection = Direction.RIGHT;
            case LEFT -> secondDirection = Direction.DOWN;
            case UP -> secondDirection = Direction.LEFT;
            case RIGHT -> secondDirection = Direction.UP;
            default -> throw new RuntimeException("iterDirection is null");
        }

        return random.nextBoolean() ? iterDirection : secondDirection;
    }

    private Direction pickRandomDirection() {
        return Direction.values()[random.nextInt(4)];
    }

    private int pickRandomShipLength() {
        return pickRandomShipLength(4);
    }
    private int pickRandomShipLength(int maxLength) {
        var tempList = nkWeighted;

        if (maxLength < 4) {
            tempList = nkWeighted.stream()
                    .filter(e -> e <= maxLength-1)
                    .toList();
            if (tempList.isEmpty()) return -1;
        }

        int k = tempList.get(random.nextInt(tempList.size()));
        removeOneShipFromList(k);

        return k+1;
    }

    private void removeOneShipFromList(int shipType) {
        Nk[shipType]--;

        if (Nk[shipType] == 0) {
            nkWeighted.removeAll(List.of(shipType));
            Nk[shipType]--;
        }
    }


    private static boolean isIntInRange(int i, int min, int max) {
        return i >= min && i <= max;
    }

    private boolean isNkEmpty() {
        return nkWeighted.isEmpty();
    }
}
