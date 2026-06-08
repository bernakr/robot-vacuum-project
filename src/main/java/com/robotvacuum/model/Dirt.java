package com.robotvacuum.model;

import com.robotvacuum.model.enums.DirtType;

public class Dirt {

    private final DirtType type;
    private int remainingTicks; // temizlenmesi için geçmesi gerekn süre 

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

    public void cleanOneTick() { // temizlenme sürelerini farklı olduğu için bu fonk kullandık 
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public boolean isCleaned() { // temizlnedi mi kontrolü 
        return remainingTicks <= 0;
    }

    public void resetProgress() { // kir temizlenmemiş gibi başa almak içn kullancağız
        remainingTicks = type.cleaningTicks();
    }
}
