package com.robotvacuum.model;

import java.util.LinkedHashSet;
import java.util.Set;

// * Oda içerisine yerleştirilecek mobilyaların şablon yapısını temsil eder.
// * Mobilyanın adı, kapladığı satır ve sütun bilgileri tutulur.
public record FurnitureTemplate(String name, int rows, int cols) {

    // * mobilya adının görünmesini sağlar.
    @Override
    public String toString() {
        return name;
    }

    // * Verilen başlangıç konumuna göre mobilyanın kapladığı tüm pozisyonları
    // hesaplar.
    public Set<Position> positionsAt(Position anchor) { // ** buralarda SET veri tipinin kullanılmaını sebebi
        return positionsAt(anchor, rows, cols); // aynı alana birden fazla eşya yada kir konulmasını engellemk için
    }

    // * Mobilya döndürülmüş şekilde yerleştirilecekse satır ve sütun değerlerini
    // değiştirir.
    public Set<Position> rotatedPositionsAt(Position anchor, boolean rotatedSideways) {
        return rotatedSideways ? positionsAt(anchor, cols, rows) : positionsAt(anchor);
    }

    // * Mobilyanın oda içerisinde kapladığı koordinatları oluşturur.
    private Set<Position> positionsAt(Position anchor, int targetRows, int targetCols) {

        // * Aynı koordinatların tekrar oluşmaması için LinkedHashSet kullanılır.
        Set<Position> positions = new LinkedHashSet<>();

        // * Mobilyanın satır boyunca kapladığı alanları dolaşır.
        for (int rowOffset = 0; rowOffset < targetRows; rowOffset++) {

            // * Mobilyanın sütun boyunca kapladığı alanları dolaşır.
            for (int colOffset = 0; colOffset < targetCols; colOffset++) {

                // * Başlangıç konumuna göre yeni koordinatlar oluşturulur.
                positions.add(
                        new Position(
                                anchor.row() + rowOffset,
                                anchor.col() + colOffset));
            }
        }

        // * Hesaplanan tüm pozisyonları geri döndürür.
        return positions;
    }
}