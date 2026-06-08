package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.robotvacuum.model.enums.CleaningAlgorithm;
import com.robotvacuum.model.enums.Direction;
import com.robotvacuum.model.enums.SimulationState;

public class Robot {

    // Robotun grid üzerindeki anlık konumu
    private Position position;

    // Robotun baktığı yön
    private Direction direction;

    // Robotun batarya bilgisini tutan model
    private final Battery battery;

    // Simülasyonun robot açısından anlık durumu
    // READY, RUNNING, PAUSED gibi değerler olabilir
    private SimulationState state;

    // Robotun kullanacağı temizlik algoritması
    // RANDOM, SPIRAL, WALL_FOLLOW gibi seçenekler olabilir
    private CleaningAlgorithm cleaningAlgorithm;

    // Robotun hareket ettiği tüm pozisyonları sırasıyla tutar
    // Ekranda mavi yol çizgisi gibi göstermek için kullanılabilir
    private final List<Position> movementPath = new ArrayList<>();

    // Robotun şarj istasyonuna geri dönüş yolunu tutar
    // BFS ile bulunan dönüş rotası burada saklanabilir
    private final List<Position> returnPath = new ArrayList<>();

    // Robotun ziyaret ettiği hücreleri tekrar etmeyecek şekilde tutar
    // LinkedHashSet kullanıldığı için hem tekrar engellenir hem eklenme sırası korunur
    private final Set<Position> visitedPositions = new LinkedHashSet<>();

    // Robotun temizlediği hücreleri tutar
    // Aynı hücrenin iki kez temizlenmiş sayılmasını engeller
    private final Set<Position> cleanedPositions = new LinkedHashSet<>();

    // Robotun şu anda temizlediği kir bilgisi
    // Kir türüne göre süre ve batarya tüketimi buradan takip edilebilir
    private Dirt activeDirt;

    // Spiral algoritmada bir yönde kaç hücre gidileceğini tutar
    private int spiralLegLength = 1;

    // Spiral algoritmada mevcut yönde kaç adım gidildiğini tutar
    private int spiralLegProgress = 0;

    // Spiral algoritmada aynı uzunlukta kaç kenar tamamlandığını tutar
    // Spiral mantığında aynı uzunluk iki kez kullanıldıktan sonra uzunluk artar
    private int spiralLegsAtLength = 0;

    // Duvar takip algoritmasının başlatılıp başlatılmadığını tutar
    private boolean wallFollowInitialized;

    // Duvar takip algoritmasında yeni hücre bulunmadan kaç adım atıldığını tutar
    private int wallFollowStepsSinceNewCell;

    // Robot ilk oluşturulduğunda temel değerler atanır
    public Robot(Position position, Direction direction, Battery battery, CleaningAlgorithm cleaningAlgorithm) {
        this.position = position;
        this.direction = direction;
        this.battery = battery;
        this.cleaningAlgorithm = cleaningAlgorithm;
        this.state = SimulationState.READY;

        // Başlangıç pozisyonu hareket yoluna eklenir
        this.movementPath.add(position);

        // Başlangıç pozisyonu ziyaret edilmiş kabul edilir
        this.visitedPositions.add(position);

        // Başlangıç pozisyonu temiz alan kabul edilir
        this.cleanedPositions.add(position);
    }

    // Robotun anlık konumunu döner
    public Position getPosition() {
        return position;
    }

    // Robotun konumunu günceller
    public void setPosition(Position position) {
        this.position = position;
    }

    // Robotun baktığı yönü döner
    public Direction getDirection() {
        return direction;
    }

    // Robotun baktığı yönü günceller
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    // Robotun batarya modelini döner
    public Battery getBattery() {
        return battery;
    }

    // Robotun simülasyon durumunu döner
    public SimulationState getState() { // başladı durduruldu gibi durumlarını göstermk için 
        return state;
    }

    // Robotun simülasyon durumunu günceller
    public void setState(SimulationState state) {
        this.state = state;
    }

    // Seçili temizlik algoritmasını döner
    public CleaningAlgorithm getCleaningAlgorithm() {
        return cleaningAlgorithm;
    }

    // Robotun temizlik algoritmasını değiştirir
    public void setCleaningAlgorithm(CleaningAlgorithm cleaningAlgorithm) {
        this.cleaningAlgorithm = cleaningAlgorithm;
    }

    // Robotun hareket yolunu dışarıya değiştirilemez liste olarak verir
    public List<Position> getMovementPath() {
        return Collections.unmodifiableList(movementPath);
    }

    // Robot yeni bir pozisyona geçtiğinde bu pozisyonu hareket ve ziyaret listesine ekler
    public void addMovementPosition(Position position) {
        movementPath.add(position);
        visitedPositions.add(position);
    }

    // Robotun ziyaret ettiği pozisyonları dışarıya değiştirilemez set olarak verir
    public Set<Position> getVisitedPositions() {
        return Collections.unmodifiableSet(visitedPositions);
    }

    // Robotun temizlediği pozisyonları dışarıya değiştirilemez set olarak verir
    public Set<Position> getCleanedPositions() {
        return Collections.unmodifiableSet(cleanedPositions);
    }

    // Verilen pozisyonu temizlenmiş olarak işaretler
    public void markCleaned(Position position) {
        if (cleanedPositions.add(position)) {
            // Yeni bir hücre temizlendiyse duvar takip algoritmasının sayaç değeri sıfırlanır
            wallFollowStepsSinceNewCell = 0;
        }
    }

    // Robotun şarj istasyonuna dönüş yolunu verir
    public List<Position> getReturnPath() {
        return Collections.unmodifiableList(returnPath);
    }

    // Robotun dönüş rotasını günceller
    public void setReturnPath(List<Position> path) {
        returnPath.clear();
        returnPath.addAll(path);
    }

    // Robotun dönüş rotasını temizler
    public void clearReturnPath() {
        returnPath.clear();
    }

    // Robotun aktif olarak temizlediği kiri döner
    public Dirt getActiveDirt() {
        return activeDirt;
    }

    // Robotun aktif olarak temizlediği kiri günceller
    public void setActiveDirt(Dirt activeDirt) {
        this.activeDirt = activeDirt;
    }

    // Spiral algoritmada mevcut bacak uzunluğunu döner
    public int getSpiralLegLength() {
        return spiralLegLength;
    }

    // Spiral algoritmada mevcut yönde kaç adım atıldığını döner
    public int getSpiralLegProgress() {
        return spiralLegProgress;
    }

    // Spiral algoritmanın ilerleme bilgisini günceller
    public void advanceSpiralProgress() {
        spiralLegProgress++;

        // Mevcut yönde yeterli adım atıldıysa yön değiştirir
        if (spiralLegProgress >= spiralLegLength) {
            spiralLegProgress = 0;
            spiralLegsAtLength++;

            // Spiral sağa dönerek ilerler
            direction = direction.turnRight();

            // Aynı uzunlukta iki kenar tamamlanınca spiral genişler
            if (spiralLegsAtLength >= 2) {
                spiralLegsAtLength = 0;
                spiralLegLength++;
            }
        }
    }

    // Spiral algoritma sayaçlarını başlangıç değerine döndürür
    public void resetSpiral() {
        spiralLegLength = 1;
        spiralLegProgress = 0;
        spiralLegsAtLength = 0;
    }

    // Duvar takip algoritması başlatıldı mı bilgisini döner
    public boolean isWallFollowInitialized() {
        return wallFollowInitialized;
    }

    // Duvar takip algoritmasının başlatılma durumunu değiştirir
    public void setWallFollowInitialized(boolean wallFollowInitialized) {
        this.wallFollowInitialized = wallFollowInitialized;
        this.wallFollowStepsSinceNewCell = 0;
    }

    // Duvar takipte yeni hücreye ulaşmadan geçen adım sayısını döner
    public int getWallFollowStepsSinceNewCell() {
        return wallFollowStepsSinceNewCell;
    }

    // Duvar takip algoritması için adım sayacını artırır
    public void advanceWallFollowStep() {
        wallFollowStepsSinceNewCell++;
    }

    // Duvar takip algoritması sayaçlarını sıfırlar
    public void resetWallFollow() {
        wallFollowInitialized = false;
        wallFollowStepsSinceNewCell = 0;
    }

    // Algoritma değiştiğinde veya simülasyon sıfırlandığında algoritmaya özel sayaçları temizler
    public void resetAlgorithmState() {
        resetSpiral();
        resetWallFollow();
    }
}