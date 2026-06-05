package com.robotvacuum.model.enums;

// Temizleme için türleri belirttik 

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

    @Override // burada override etmemizin sebebi Random olarak algoritmanın adını yazmak  yerine
    // atama yaptığımız Rastegele gibi isimleri döndürmek için
    public String toString() {
        return displayName;
    }
}
