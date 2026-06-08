package com.robotvacuum.model;

import com.robotvacuum.model.enums.Direction;

/* Recordu Küçük veri objeleri için kullan burada row col için fonksiyonlara vs yazmkatan kurtarır otomaitk veriyor onları

bir CLASS türüdür
özel bir Java sınıfıdır
immutable (değiştirilemez) veri objesi üretmek için kullanılır

 */
public record Position(int row, int col) {

    public Position move(Direction direction) {
        return new Position(
                row + direction.rowDelta(),
                col + direction.colDelta());
    }
}