package com.robotvacuum.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Obstacle { // eşylarımız için olan modelimiz 

    // Mobilya / engelin adı
    // Örneğin: Koltuk, Masa, Dolap
    private final String name;

    // Mobilyanın grid üzerinde kapladığı tüm hücreler
    // Set kullanıldığı için aynı pozisyon iki kez eklenemez
    // LinkedHashSet kullanıldığı için eklenme sırası korunur BÜTÜNLÜĞÜ DE KORUNMUŞ OLUR 
    private Set<Position> positions;

    // Mobilyanın dönüş açısı
    // 0, 90, 180, 270 gibi değerler tutulabilir
    private int rotationDegrees;

    // Obstacle oluşturulurken adı ve kapladığı hücreler atanır
    public Obstacle(String name, Set<Position> positions) {

        this.name = name;

        // Dışarıdan gelen Set'in referansını direkt almak yerine
        // yeni LinkedHashSet oluşturularak veri güvenliği sağlanır
        this.positions = new LinkedHashSet<>(positions);
    }

    // Mobilyanın adını döner
    public String getName() {
        return name;
    }

    // Mobilyanın kapladığı hücreleri dışarıya salt okunur şekilde verir
    // Böylece dışarıdan positions seti değiştirilemez
    public Set<Position> getPositions() {
        return Collections.unmodifiableSet(positions);
    }

    // Mobilyanın mevcut dönüş açısını döner
    public int getRotationDegrees() {
        return rotationDegrees;
    }

    // Mobilyanın kapladığı hücreleri günceller
    // package-private bırakılmış
    // Sadece aynı package içindeki sınıflar değiştirebilir
    void setPositions(Set<Position> positions) {

        // Yeni referans yerine kopya oluşturulur
        // Böylece dış müdahale engellenir
        this.positions = new LinkedHashSet<>(positions);
    }

    // Mobilyanın dönüş açısını günceller
    void setRotationDegrees(int rotationDegrees) {

        // Açıyı her zaman 0-359 aralığında tutar
        // Örneğin:
        // 450 -> 90
        // -90 -> 270
        this.rotationDegrees = Math.floorMod(rotationDegrees, 360);
    }

    // Verilen pozisyon bu mobilyanın içinde mi kontrol eder
    public boolean contains(Position position) {
        return positions.contains(position);
    }
}

// HashSet --> tekrar eden veriyi engelliyor ama sıra olayını korumuyor 
// LİnkHashSet --> hashsete özel ayıyeten sırayı da korur 