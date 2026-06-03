package com.robotvacuum.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record FurnitureTemplate(String name, int rows, int cols) {
    @Override
    public String toString() {
        return name;
    }

    public Set<Position> positionsAt(Position anchor) {
        return positionsAt(anchor, rows, cols);
    }

    public Set<Position> rotatedPositionsAt(Position anchor, boolean rotatedSideways) {
        return rotatedSideways ? positionsAt(anchor, cols, rows) : positionsAt(anchor);
    }

    private Set<Position> positionsAt(Position anchor, int targetRows, int targetCols) {
        Set<Position> positions = new LinkedHashSet<>();
        for (int rowOffset = 0; rowOffset < targetRows; rowOffset++) {
            for (int colOffset = 0; colOffset < targetCols; colOffset++) {
                positions.add(new Position(anchor.row() + rowOffset, anchor.col() + colOffset));
            }
        }
        return positions;
    }
}
