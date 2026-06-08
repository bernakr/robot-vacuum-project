package com.robotvacuum.model;

import com.robotvacuum.model.enums.CellType;

public class Cell {

    // Hücrenin grid üzerindeki konumu
    private final Position position;

    // Hücrede kir varsa bu alan dolu olur
    // Kir yoksa null kalır
    private Dirt dirt;

    // Hücrede mobilya/engel varsa bu alan dolu olur
    // Engel yoksa null kalır
    private Obstacle obstacle;

    // Bu hücre şarj istasyonu mu bilgisini tutar
    private boolean chargingStation;

    // Cell oluşturulurken hangi pozisyona ait olduğu belirlenir
    public Cell(Position position) {
        this.position = position;
    }

    // Hücrenin konumunu döner
    public Position getPosition() {
        return position;
    }

    // Hücredeki kir nesnesini döner
    public Dirt getDirt() {
        return dirt;
    }

    // Hücreye kir ekler veya kiri temizlemek için null yapar
    public void setDirt(Dirt dirt) {
        this.dirt = dirt;
    }

    // Hücredeki mobilya/engel nesnesini döner
    public Obstacle getObstacle() {
        return obstacle;
    }

    // Hücreye mobilya/engel ekler veya kaldırmak için null yapar
    public void setObstacle(Obstacle obstacle) {
        this.obstacle = obstacle;
    }

    // Hücrede mobilya/engel var mı kontrol eder
    public boolean hasObstacle() {
        return obstacle != null;
    }

    // Hücrede kir var mı kontrol eder
    public boolean hasDirt() {
        return dirt != null;
    }

    // Hücre şarj istasyonu mu kontrol eder
    public boolean isChargingStation() {
        return chargingStation;
    }

    // Hücreyi şarj istasyonu olarak işaretler veya kaldırır
    public void setChargingStation(boolean chargingStation) {
        this.chargingStation = chargingStation;
    }

    // Robotun bu hücreye girip giremeyeceğini kontrol eder
    // Şu an sadece mobilya/engel varsa geçilemez kabul ediliyor
    public boolean isPassable() {
        return obstacle == null;
    }

    // Hücrenin ekranda hangi tipte gösterileceğini belirler
    // Öncelik sırası:
    // 1. Şarj istasyonu
    // 2. Engel/mobilya
    // 3. Kir
    // 4. Boş hücre
    public CellType getType() {
        if (chargingStation) {
            return CellType.CHARGING_STATION;
        }

        if (obstacle != null) {
            return CellType.OBSTACLE;
        }

        if (dirt != null) {
            return CellType.DIRTY;
        }

        return CellType.EMPTY;
    }
}