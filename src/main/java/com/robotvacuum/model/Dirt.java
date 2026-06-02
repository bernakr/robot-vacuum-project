package com.robotvacuum.model;

import com.robotvacuum.model.enums.DirtType;

public class Dirt {
    private final DirtType type;
    private int remainingTicks;

    public Dirt(DirtType type) {
        this.type = type;
        this.remainingTicks = type.cleaningTicks();
    }

    public DirtType getType() {
        return type;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void cleanOneTick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public boolean isCleaned() {
        return remainingTicks <= 0;
    }

    public void resetProgress() {
        remainingTicks = type.cleaningTicks();
    }
}
