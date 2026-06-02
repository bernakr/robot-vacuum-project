package com.robotvacuum.model;

import com.robotvacuum.model.enums.DirtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Room {
    private final int rows;
    private final int cols;
    private final Cell[][] grid;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private int totalDirtTarget;
    private int collectedDirtCount;
    private ChargingStation chargingStation;

    public Room(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid[row][col] = new Cell(new Position(row, col));
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public boolean isInside(Position position) {
        return position.row() >= 0 && position.row() < rows
                && position.col() >= 0 && position.col() < cols;
    }

    public Cell getCell(Position position) {
        if (!isInside(position)) {
            throw new IllegalArgumentException("Grid dışı konum: " + position);
        }
        return grid[position.row()][position.col()];
    }

    public boolean isPassable(Position position) {
        return isInside(position) && getCell(position).isPassable();
    }

    public boolean isPassableForCleaning(Position position) {
        return isPassable(position) && !getCell(position).isChargingStation();
    }

    public boolean isPassableForPath(Position position, Position allowedChargingStationGoal) {
        if (!isPassable(position)) {
            return false;
        }
        if (!getCell(position).isChargingStation()) {
            return true;
        }
        return allowedChargingStationGoal != null && position.equals(allowedChargingStationGoal);
    }

    public Optional<String> addDirt(Position position, DirtType dirtType, Position robotPosition) {
        return addDirt(position, dirtType, robotPosition, Set.of());
    }

    public Optional<String> addDirt(
            Position position,
            DirtType dirtType,
            Position robotPosition,
            Set<Position> cleanedPositions
    ) {
        Optional<String> error = validateDirtPlacement(position, robotPosition, cleanedPositions);
        if (error.isPresent()) {
            return error;
        }
        getCell(position).setDirt(new Dirt(dirtType));
        totalDirtTarget++;
        return Optional.empty();
    }

    public Optional<String> validateDirtPlacement(
            Position position,
            Position robotPosition,
            Set<Position> cleanedPositions
    ) {
        if (!isInside(position)) {
            return Optional.of("Grid dışına kir eklenemez.");
        }
        Cell cell = getCell(position);
        if (position.equals(robotPosition)) {
            return Optional.of("Robotun bulunduğu hücreye kir eklenemez.");
        }
        if (cleanedPositions.contains(position)) {
            return Optional.of("Daha önce temizlenmiş hücreye kir eklenemez.");
        }
        if (cell.isChargingStation()) {
            return Optional.of("Şarj istasyonuna kir eklenemez.");
        }
        if (cell.hasObstacle()) {
            return Optional.of("Mobilya bulunan hücreye kir eklenemez.");
        }
        if (cell.hasDirt()) {
            return Optional.of("Aynı hücrede ikinci kir olamaz.");
        }
        return Optional.empty();
    }

    public Optional<String> addObstacle(String name, Set<Position> positions, Position robotPosition) {
        return addObstacle(name, positions, robotPosition, Set.of());
    }

    public Optional<String> addObstacle(
            String name,
            Set<Position> positions,
            Position robotPosition,
            Set<Position> cleanedPositions
    ) {
        Optional<String> error = validateObstaclePlacement(positions, robotPosition, cleanedPositions);
        if (error.isPresent()) {
            return error;
        }

        Obstacle obstacle = new Obstacle(name, positions);
        obstacles.add(obstacle);
        for (Position position : positions) {
            getCell(position).setObstacle(obstacle);
        }
        return Optional.empty();
    }

    public Optional<String> validateObstaclePlacement(
            Set<Position> positions,
            Position robotPosition,
            Set<Position> cleanedPositions
    ) {
        for (Position position : positions) {
            if (!isInside(position)) {
                return Optional.of("Grid dışına mobilya eklenemez.");
            }
            Cell cell = getCell(position);
            if (position.equals(robotPosition)) {
                return Optional.of("Robotun bulunduğu hücreye mobilya eklenemez.");
            }
            if (cleanedPositions.contains(position)) {
                return Optional.of("Daha önce temizlenmiş hücreye mobilya eklenemez.");
            }
            if (cell.isChargingStation()) {
                return Optional.of("Şarj istasyonuna mobilya eklenemez.");
            }
            if (cell.hasObstacle()) {
                return Optional.of("Bu hücrede zaten mobilya var.");
            }
            if (cell.hasDirt()) {
                return Optional.of("Kir bulunan hücreye mobilya eklenemez.");
            }
        }
        return Optional.empty();
    }

    public void removeObstacleAt(Position position) {
        if (!isInside(position)) {
            return;
        }
        Obstacle obstacle = getCell(position).getObstacle();
        if (obstacle == null) {
            return;
        }
        obstacles.remove(obstacle);
        for (Position obstaclePosition : obstacle.getPositions()) {
            getCell(obstaclePosition).setObstacle(null);
        }
    }

    public void cleanDirt(Position position) {
        if (isInside(position)) {
            Cell cell = getCell(position);
            if (cell.hasDirt()) {
                cell.setDirt(null);
                collectedDirtCount++;
            }
        }
    }

    public int getTotalDirtTarget() {
        return totalDirtTarget;
    }

    public int getCollectedDirtCount() {
        return collectedDirtCount;
    }

    public List<Dirt> getActiveDirt() {
        List<Dirt> dirt = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Dirt cellDirt = grid[row][col].getDirt();
                if (cellDirt != null) {
                    dirt.add(cellDirt);
                }
            }
        }
        return dirt;
    }

    public List<Obstacle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public ChargingStation getChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(ChargingStation chargingStation) {
        this.chargingStation = chargingStation;
        getCell(chargingStation.getPosition()).setChargingStation(true);
    }
}
