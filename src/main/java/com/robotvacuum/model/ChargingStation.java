package com.robotvacuum.model;

// bu ilk aşamada Şarj istasyonunu sabit tuttup kullanıcının değişitrmesini istemedğimiz için SET  fonksiyonunu eklemedik

public class ChargingStation {
    private final Position position;

    public ChargingStation(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
