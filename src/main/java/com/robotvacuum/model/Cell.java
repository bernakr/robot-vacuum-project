package com.robotvacuum.model;

import com.robotvacuum.model.enums.CellType;

public class Cell {
    private final Position position;
    private Dirt dirt;
    private Obstacle obstacle;
    private boolean chargingStation;

    public Cell(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Dirt getDirt() {
        return dirt;
    }

    public void setDirt(Dirt dirt) {
        this.dirt = dirt;
    }

    public Obstacle getObstacle() {
        return obstacle;
    }

    public void setObstacle(Obstacle obstacle) {
        this.obstacle = obstacle;
    }

    public boolean hasObstacle() {
        return obstacle != null;
    }

    public boolean hasDirt() {
        return dirt != null;
    }

    public boolean isChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(boolean chargingStation) {
        this.chargingStation = chargingStation;
    }

    public boolean isPassable() {
        return obstacle == null;
    }

    public CellType getType() {
        if (chargingStation) {
            return CellType.CHARGING_STATION;
        }
        if (obstacle != null) {
            return CellType.OBSTACLE;
        }
        if (dirt != null) {
            return CellType.DIRTY;
        }
        return CellType.EMPTY;
    }
}
