package com.robotvacuum.model;

public class Battery {
    private double level;

    public Battery(double initialLevel) {

        if (initialLevel < 0 || initialLevel > 100) {
            throw new IllegalArgumentException(
                    "Batarya değeri 0-100 arasında olmalıdır.");
        }

        this.level = initialLevel;
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

    public void consume(double amount) { // pil durumunun azalma ve artırma yapıları için 
        level = Math.max(0, level - Math.max(0, amount));
    }

    public void charge(double amount) {
        level = Math.min(100, level + Math.max(0, amount));
    }

    public boolean isFull() { // batarya doluluk kontrolü için 
        return level >= 100;
    }
}
