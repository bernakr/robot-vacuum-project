package com.robotvacuum.model;

public class Battery {
    private double level;

    public Battery(double initialLevel) {
        setLevel(initialLevel);
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("Batarya değeri 0-100 arasında olmalıdır.");
        }
        this.level = level;
    }

    public void consume(double amount) {
        level = Math.max(0, level - Math.max(0, amount));
    }

    public void charge(double amount) {
        level = Math.min(100, level + Math.max(0, amount));
    }

    public boolean isFull() {
        return level >= 100;
    }
}
