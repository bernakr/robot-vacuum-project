package com.robotvacuum.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Obstacle {
    private final String name;
    private Set<Position> positions;
    private int rotationDegrees;

    public Obstacle(String name, Set<Position> positions) {
        this.name = name;
        this.positions = new LinkedHashSet<>(positions);
    }

    public String getName() {
        return name;
    }

    public Set<Position> getPositions() {
        return Collections.unmodifiableSet(positions);
    }

    public int getRotationDegrees() {
        return rotationDegrees;
    }

    void setPositions(Set<Position> positions) {
        this.positions = new LinkedHashSet<>(positions);
    }

    void setRotationDegrees(int rotationDegrees) {
        this.rotationDegrees = Math.floorMod(rotationDegrees, 360);
    }

    public boolean contains(Position position) {
        return positions.contains(position);
    }
}
