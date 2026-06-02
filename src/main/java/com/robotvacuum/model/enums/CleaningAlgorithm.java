package com.robotvacuum.model.enums;

public enum CleaningAlgorithm {
    RANDOM("Rastgele"),
    SPIRAL("Spiral"),
    WALL_FOLLOW("Duvar Takip");

    private final String displayName;

    CleaningAlgorithm(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
