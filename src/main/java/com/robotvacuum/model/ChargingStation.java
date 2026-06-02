package com.robotvacuum.model;

public class ChargingStation {
    private final Position position;

    public ChargingStation(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
