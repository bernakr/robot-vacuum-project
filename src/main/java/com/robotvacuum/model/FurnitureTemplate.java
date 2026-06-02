package com.robotvacuum.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record FurnitureTemplate(String name, int rows, int cols) {
    public Set<Position> positionsAt(Position anchor) {
        Set<Position> positions = new LinkedHashSet<>();
        for (int rowOffset = 0; rowOffset < rows; rowOffset++) {
            for (int colOffset = 0; colOffset < cols; colOffset++) {
                positions.add(new Position(anchor.row() + rowOffset, anchor.col() + colOffset));
            }
        }
        return positions;
    }
}
