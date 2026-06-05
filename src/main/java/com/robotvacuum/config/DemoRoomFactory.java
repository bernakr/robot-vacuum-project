package com.robotvacuum.config;

import java.util.LinkedHashSet;
import java.util.Set;

import com.robotvacuum.model.ChargingStation;
import com.robotvacuum.model.Position;
import com.robotvacuum.model.Room;
import com.robotvacuum.model.enums.DirtType;

//  * Bu sınıf uygulama ilk açıldığında oda görüntüsünü oluşturmamızı sağlar default yapıların yani 
public final class DemoRoomFactory {

    /**
     * Bu sınıftan nesne oluşturulmasını engeller.
     *
     * Çünkü bütün işlemler static yapılacak.
     */
    private DemoRoomFactory() {
    }

    // * Varsayılan demo odasını oluşturur.
    public static Room createRoom() {

        /**
         * SimulationConfig içindeki
         * satır ve sütun değerlerine göre oda oluşturulur.
         */
        Room room = new Room(
                SimulationConfig.ROWS,
                SimulationConfig.COLS);

        /**
         * Odaya şarj istasyonu ekleniyor.
         */
        room.setChargingStation(
                new ChargingStation(
                        SimulationConfig.CHARGING_STATION_POSITION));

        /**
         * addRectangle(...)
         *
         * Verilen koordinatlar arasında
         * dikdörtgen şeklinde engel oluşturur.
         */

        addRectangle(room, "Koltuk", 2, 5, 3, 8);
        addRectangle(room, "Orta Masa", 5, 5, 6, 8);
        addRectangle(room, "Tekli Koltuk", 5, 2, 6, 3);
        addRectangle(room, "Yemek Masası", 5, 14, 7, 16);
        addRectangle(room, "Dolap", 9, 18, 10, 19);
        addRectangle(room, "Bitki", 1, 0, 1, 0);
        addRectangle(room, "Bitki", 11, 19, 11, 19);

        // * Verilen koordinata belirtilen kir tipini ekleyelim
        // Toz kirleri
        addDirt(room, 1, 3, DirtType.DUST);
        addDirt(room, 3, 15, DirtType.DUST);
        addDirt(room, 8, 4, DirtType.DUST);

        // Sıvı kirleri
        addDirt(room, 4, 10, DirtType.LIQUID);
        addDirt(room, 9, 12, DirtType.LIQUID);

        // Leke kirleri
        addDirt(room, 2, 18, DirtType.STAIN);
        addDirt(room, 7, 11, DirtType.STAIN);

        /**
         * Hazır oluşturulan oda geri döndürülür.
         */
        return room;
    }

    /**
     * Dikdörtgen şeklinde engel / eşya oluşturur.
     *
     * Parametreler:
     *
     * room -> işlem yapılacak oda
     * name -> eşya adı
     * startRow -> başlangıç satırı
     * startCol -> başlangıç sütunu
     * endRow -> bitiş satırı
     * endCol -> bitiş sütunu
     */
    private static void addRectangle(
            Room room,
            String name,
            int startRow,
            int startCol,
            int endRow,
            int endCol) {

        /**
         * Eşyanın kapladığı tüm koordinatlar burada tutulur.
         */
        Set<Position> positions = new LinkedHashSet<>(); // bir daha oraya eşya eklenmemesi için Set yapısını kullandık

        /**
         * Satırları dolaşıyoruz.
         */
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                positions.add(new Position(row, col));
            }
        }

        /**
         * Oluşturulan tüm koordinatlar
         * obstacle / eşya olarak odaya eklenir.
         */
        room.addObstacle(
                name,
                positions,
                SimulationConfig.ROBOT_START_POSITION);
    }

    // * Tek bir hücreye kir ekler.
    private static void addDirt(
            Room room,
            int row,
            int col,
            DirtType type) {

        /**
         * Verilen koordinata kir ekleniyor.
         */
        room.addDirt(
                new Position(row, col),
                type,
                SimulationConfig.ROBOT_START_POSITION);
    }
}