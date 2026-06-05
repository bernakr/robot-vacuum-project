package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.robotvacuum.model.enums.DirtType;

public class Room {

    // Odanın satır sayısı
    private final int rows;

    // Odanın sütun sayısı
    private final int cols;

    // Odanın hücre matrisi
    // Her kare bir Cell nesnesidir
    private final Cell[][] grid;

    // Odaya eklenen tüm mobilya/engel nesnelerini tutan liste
    // List<Obstacle> kullanarak sadece Obstacle türündeki nesnelerin eklenmesini
    // sağlıyoruz
    private final List<Obstacle> obstacles = new ArrayList<>();

    // Simülasyon boyunca eklenen toplam kir hedefi
    private int totalDirtTarget;

    // Robotun temizlediği kir sayısı
    private int collectedDirtCount;

    // Odadaki şarj istasyonu
    private ChargingStation chargingStation;

    // Oda oluşturulurken satır ve sütun değerlerine göre grid hazırlanır
    public Room(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];

        // Her koordinat için boş bir Cell oluşturulur
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid[row][col] = new Cell(new Position(row, col));
            }
        }
    }

    // Odanın satır sayısını döner
    public int getRows() {
        return rows;
    }

    // Odanın sütun sayısını döner
    public int getCols() {
        return cols;
    }

    // Verilen pozisyon grid sınırları içinde mi kontrol eder
    // burada bunu eşya eklerken kir eklerken ve robotun ypaımız yani cell dışına
    // çıkmaması için kullandık
    public boolean isInside(Position position) {
        return position.row() >= 0 && position.row() < rows
                && position.col() >= 0 && position.col() < cols;
    }

    // Verilen pozisyondaki hücreyi döner
    // Eğer pozisyon grid dışındaysa hata fırlatır
    public Cell getCell(Position position) {
        if (!isInside(position)) {
            throw new IllegalArgumentException("Grid dışı konum: " + position);
        }
        return grid[position.row()][position.col()];
    }

    // Robot bu pozisyona geçebilir mi kontrol eder
    public boolean isPassable(Position position) {
        return isInside(position) && getCell(position).isPassable();
    }

    // Temizlik sırasında robotun geçebileceği alanı kontrol eder
    // Şarj istasyonu temizlik rotasında hedef değilse dışlanır
    public boolean isPassableForCleaning(Position position) {
        return isPassable(position) && !getCell(position).isChargingStation();
    }

    // BFS gibi yol bulma algoritmalarında kullanılır
    // Normalde şarj istasyonuna girilmez
    // Ama hedef şarj istasyonunun kendisiyse izin verilir
    public boolean isPassableForPath(Position position, Position allowedChargingStationGoal) {
        if (!isPassable(position)) {
            return false;
        }

        if (!getCell(position).isChargingStation()) {
            return true;
        }

        return allowedChargingStationGoal != null && position.equals(allowedChargingStationGoal);
    }

    // Basit kir ekleme metodu
    // Daha önce temizlenen pozisyon listesi yoksa boş Set gönderir
    public Optional<String> addDirt(Position position, DirtType dirtType, Position robotPosition) {
        return addDirt(position, dirtType, robotPosition, Set.of());
    }

    // Belirli bir pozisyona kir ekler
    // Önce yerleştirme geçerli mi diye kontrol eder
    public Optional<String> addDirt(
            Position position,
            DirtType dirtType,
            Position robotPosition,
            Set<Position> cleanedPositions) {
        Optional<String> error = validateDirtPlacement(position, robotPosition, cleanedPositions);

        if (error.isPresent()) {
            return error;
        }

        getCell(position).setDirt(new Dirt(dirtType));
        totalDirtTarget++;

        return Optional.empty();
    }

    // Kir eklenmek istenen hücrenin uygun olup olmadığını kontrol eder
    public Optional<String> validateDirtPlacement(
            Position position,
            Position robotPosition,
            Set<Position> cleanedPositions) {
        if (!isInside(position)) {
            return Optional.of("Grid dışına kir eklenemez.");
        }

        Cell cell = getCell(position);

        if (position.equals(robotPosition)) {
            return Optional.of("Robotun bulunduğu hücreye kir eklenemez.");
        }

        if (cleanedPositions.contains(position)) {
            return Optional.of("Daha önce temizlenmiş hücreye kir eklenemez.");
        }

        if (cell.isChargingStation()) {
            return Optional.of("Şarj istasyonuna kir eklenemez.");
        }

        if (cell.hasObstacle()) {
            return Optional.of("Mobilya bulunan hücreye kir eklenemez.");
        }

        if (cell.hasDirt()) {
            return Optional.of("Aynı hücrede ikinci kir olamaz.");
        }

        return Optional.empty();
    }

    // Var olan kiri bir hücreden başka bir hücreye taşır
    // Drag/drop gibi işlemlerde kullanılabilir
    public Optional<String> moveDirt(
            Position source,
            Position target,
            Position robotPosition,
            Set<Position> cleanedPositions) {
        if (!isInside(source) || !isInside(target)) {
            return Optional.of("Kir grid dışına taşınamaz.");
        }

        if (source.equals(target)) {
            return Optional.empty();
        }

        Cell sourceCell = getCell(source);
        Dirt dirt = sourceCell.getDirt();

        if (dirt == null) {
            return Optional.of("Taşınacak kir bulunamadı.");
        }

        // Önce kaynak hücredeki kiri geçici olarak kaldırıyoruz
        sourceCell.setDirt(null);

        // Hedef hücre kir için uygun mu kontrol ediyoruz
        Optional<String> error = validateDirtPlacement(target, robotPosition, cleanedPositions);

        if (error.isPresent()) {
            // Hedef uygun değilse kiri eski yerine geri koyuyoruz
            sourceCell.setDirt(dirt);
            return error;
        }

        getCell(target).setDirt(dirt);

        return Optional.empty();
    }

    // Basit mobilya / engel ekleme metodu
    public Optional<String> addObstacle(String name, Set<Position> positions, Position robotPosition) {
        return addObstacle(name, positions, robotPosition, Set.of());
    }

    // Birden fazla hücre kaplayan mobilya / engel ekler
    public Optional<String> addObstacle(
            String name,
            Set<Position> positions,
            Position robotPosition,
            Set<Position> cleanedPositions) {
        Optional<String> error = validateObstaclePlacement(positions, robotPosition, cleanedPositions);

        if (error.isPresent()) {
            return error;
        }

        Obstacle obstacle = new Obstacle(name, positions);
        obstacles.add(obstacle);

        // Mobilyanın kapladığı tüm hücrelere aynı obstacle referansı yazılır
        for (Position position : positions) {
            getCell(position).setObstacle(obstacle);
        }

        return Optional.empty();
    }

    // Mobilya yerleştirme kontrolü
    public Optional<String> validateObstaclePlacement(
            Set<Position> positions,
            Position robotPosition,
            Set<Position> cleanedPositions) {
        return validateObstaclePlacement(positions, robotPosition, cleanedPositions, null);
    }

    // Mobilya yerleştirme kontrolü
    // ignoredObstacle parametresi mobilya taşıma/döndürme sırasında kendi eski
    // hücrelerini yok saymak için kullanılır
    public Optional<String> validateObstaclePlacement(
            Set<Position> positions,
            Position robotPosition,
            Set<Position> cleanedPositions,
            Obstacle ignoredObstacle) {
        for (Position position : positions) {
            if (!isInside(position)) {
                return Optional.of("Grid dışına mobilya eklenemez.");
            }

            Cell cell = getCell(position);

            if (position.equals(robotPosition)) {
                return Optional.of("Robotun bulunduğu hücreye mobilya eklenemez.");
            }

            if (cleanedPositions.contains(position)) {
                return Optional.of("Daha önce temizlenmiş hücreye mobilya eklenemez.");
            }

            if (cell.isChargingStation()) {
                return Optional.of("Şarj istasyonuna mobilya eklenemez.");
            }

            if (cell.hasObstacle() && cell.getObstacle() != ignoredObstacle) {
                return Optional.of("Bu hücrede zaten mobilya var.");
            }

            if (cell.hasDirt()) {
                return Optional.of("Kir bulunan hücreye mobilya eklenemez.");
            }
        }

        return Optional.empty();
    }

    // Belirli bir pozisyonda mobilya / engel var mı bulur
    public Optional<Obstacle> findObstacleAt(Position position) {
        if (!isInside(position)) {
            return Optional.empty();
        }

        return Optional.ofNullable(getCell(position).getObstacle());
    }

    // Var olan bir mobilyanın pozisyonlarını değiştirir
    // Taşıma veya döndürme işlemlerinde kullanılır
    public void replaceObstaclePositions(Obstacle obstacle, Set<Position> newPositions, int rotationDegrees) {
        // Eski hücrelerden obstacle bilgisini kaldır
        for (Position position : obstacle.getPositions()) {
            getCell(position).setObstacle(null);
        }

        // Obstacle modelini yeni pozisyonlarla güncelle
        obstacle.setPositions(newPositions);
        obstacle.setRotationDegrees(rotationDegrees);

        // Yeni hücrelere obstacle bilgisini yaz
        for (Position position : newPositions) {
            getCell(position).setObstacle(obstacle);
        }
    }

    // Belirli bir pozisyondaki mobilyayı odadan siler
    public void removeObstacleAt(Position position) {
        if (!isInside(position)) {
            return;
        }

        Obstacle obstacle = getCell(position).getObstacle();

        if (obstacle == null) {
            return;
        }

        obstacles.remove(obstacle);

        // Mobilya birden fazla hücre kapladığı için hepsinden temizlenir
        for (Position obstaclePosition : obstacle.getPositions()) {
            getCell(obstaclePosition).setObstacle(null);
        }
    }

    // Robot bu pozisyona geldiğinde kir varsa temizler
    public void cleanDirt(Position position) {
        if (isInside(position)) {
            Cell cell = getCell(position);

            if (cell.hasDirt()) {
                cell.setDirt(null);
                collectedDirtCount++;
            }
        }
    }

    // Toplam eklenen kir sayısını döner
    public int getTotalDirtTarget() {
        return totalDirtTarget;
    }

    // Temizlenen kir sayısını döner
    public int getCollectedDirtCount() {
        return collectedDirtCount;
    }

    // Odada hâlâ temizlenmemiş kirleri listeler
    public List<Dirt> getActiveDirt() {
        List<Dirt> dirt = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Dirt cellDirt = grid[row][col].getDirt();

                if (cellDirt != null) {
                    dirt.add(cellDirt);
                }
            }
        }

        return dirt;
    }

    // Odadaki mobilyaların kapladığı toplam hücre sayısını döner.
    // Robot bu alanlara giremediği için bu alan "kullanılamayan alan" sayılır.
    public int getObstacleArea() {
        return obstacles.stream()
                .mapToInt(obstacle -> obstacle.getPositions().size())
                .sum();
    }

    // Robotun temizleyebileceği gerçek alanı döner.
    // Toplam alandan mobilya/engel alanları çıkarılır.
    public int getCleanableArea() {
        return (rows * cols) - getObstacleArea();
    }

    // Mobilyaları isimlerine göre gruplayıp kapladıkları toplam alanı döner.
    // Örnek: Koltuk=8, Bitki=2, Masa=17
    public Map<String, Integer> getObstacleAreaByName() {
        Map<String, Integer> areaByName = new LinkedHashMap<>();

        for (Obstacle obstacle : obstacles) {
            areaByName.merge(
                    obstacle.getName(),
                    obstacle.getPositions().size(),
                    Integer::sum);
        }

        return areaByName;
    }

    // Odadaki mobilya / engel listesini dışarıya salt okunur şekilde verir
    public List<Obstacle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    // Şarj istasyonunu döner
    public ChargingStation getChargingStation() {
        return chargingStation;
    }

    // Şarj istasyonunu odaya ekler
    // Ayrıca ilgili hücreyi chargingStation olarak işaretler
    public void setChargingStation(ChargingStation chargingStation) {
        this.chargingStation = chargingStation;
        getCell(chargingStation.getPosition()).setChargingStation(true);
    }
}