package com.robotvacuum.model.enums;

public enum DirtType {
    DUST("Toz", 1, 2.0),
    LIQUID("Sıvı", 3, 4.0),
    STAIN("Leke", 5, 6.0);

    private final String displayName;
    private final int cleaningTicks;
    private final double batteryCost;

    DirtType(String displayName, int cleaningTicks, double batteryCost) {
        this.displayName = displayName;
        this.cleaningTicks = cleaningTicks;
        this.batteryCost = batteryCost;
    }

    public String displayName() {
        return displayName;
    }

    public int cleaningTicks() {
        return cleaningTicks;
    }

    public double batteryCost() {
        return batteryCost;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
