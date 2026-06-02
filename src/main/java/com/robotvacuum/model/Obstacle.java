package com.robotvacuum.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Obstacle {
    private final String name;
    private final Set<Position> positions;

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

    public boolean contains(Position position) {
        return positions.contains(position);
    }
}
