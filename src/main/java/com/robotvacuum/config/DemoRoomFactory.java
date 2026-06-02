package com.robotvacuum.config;

import com.robotvacuum.model.ChargingStation;
import com.robotvacuum.model.Position;
import com.robotvacuum.model.Room;
import com.robotvacuum.model.enums.DirtType;

import java.util.LinkedHashSet;
import java.util.Set;

public final class DemoRoomFactory {
    private DemoRoomFactory() {
    }

    public static Room createRoom() {
        Room room = new Room(SimulationConfig.ROWS, SimulationConfig.COLS);
        room.setChargingStation(new ChargingStation(SimulationConfig.CHARGING_STATION_POSITION));

        addRectangle(room, "Koltuk", 2, 5, 3, 8);
        addRectangle(room, "Orta Masa", 5, 5, 6, 8);
        addRectangle(room, "Tekli Koltuk", 5, 2, 6, 3);
        addRectangle(room, "Yemek Masası", 5, 14, 7, 16);
        addRectangle(room, "Dolap", 9, 18, 10, 19);
        addRectangle(room, "Bitki", 1, 0, 1, 0);
        addRectangle(room, "Bitki", 11, 19, 11, 19);

        addDirt(room, 1, 3, DirtType.DUST);
        addDirt(room, 3, 15, DirtType.DUST);
        addDirt(room, 8, 4, DirtType.DUST);
        addDirt(room, 4, 10, DirtType.LIQUID);
        addDirt(room, 9, 12, DirtType.LIQUID);
        addDirt(room, 2, 18, DirtType.STAIN);
        addDirt(room, 7, 11, DirtType.STAIN);

        return room;
    }

    private static void addRectangle(Room room, String name, int startRow, int startCol, int endRow, int endCol) {
        Set<Position> positions = new LinkedHashSet<>();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                positions.add(new Position(row, col));
            }
        }
        room.addObstacle(name, positions, SimulationConfig.ROBOT_START_POSITION);
    }

    private static void addDirt(Room room, int row, int col, DirtType type) {
        room.addDirt(new Position(row, col), type, SimulationConfig.ROBOT_START_POSITION);
    }
}
