package com.robotvacuum.model.enums;

public enum Direction {
    UP(-1, 0, "Yukarı"),
    RIGHT(0, 1, "Sağ"),
    DOWN(1, 0, "Aşağı"),
    LEFT(0, -1, "Sol");

    private final int rowDelta;
    private final int colDelta;
    private final String displayName;

    Direction(int rowDelta, int colDelta, String displayName) {
        this.rowDelta = rowDelta;
        this.colDelta = colDelta;
        this.displayName = displayName;
    }

    public int rowDelta() {
        return rowDelta;
    }

    public int colDelta() {
        return colDelta;
    }

    public String displayName() {
        return displayName;
    }

    public Direction turnRight() {
        return values()[(ordinal() + 1) % values().length];
    }

    public Direction turnLeft() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public Direction reverse() {
        return values()[(ordinal() + 2) % values().length];
    }

    public static Direction fromDelta(int rowDelta, int colDelta) {
        for (Direction direction : values()) {
            if (direction.rowDelta == rowDelta && direction.colDelta == colDelta) {
                return direction;
            }
        }
        return RIGHT;
    }
}
