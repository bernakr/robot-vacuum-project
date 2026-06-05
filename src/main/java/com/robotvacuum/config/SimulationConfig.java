package com.robotvacuum.config;

import com.robotvacuum.model.Position;

// * Simülasyonun genel ayarlarını ve sabit değerlerini tutar.
public final class SimulationConfig {
    public static final int ROWS = 12;
    public static final int COLS = 20;
    public static final int INITIAL_BATTERY = 100; // * Robotun başlangıç pil seviyesi.
    public static final int LOW_BATTERY_THRESHOLD = 20; // * Düşük pil seviyesi eşiği.
    public static final double MOVEMENT_BATTERY_COST = 1.0; // * Her hareketin pil maliyeti.
    public static final int MOVES_PER_BATTERY_COST = 2; // * Kaç harekette bir pil azaltılacağını belirtir.
    public static final double CHARGE_PER_TICK = 4.0; // * Robot şarj olurken her tickte kazanacağı pil miktarı.
    public static final Position CHARGING_STATION_POSITION = new Position(10, 0);
    public static final Position ROBOT_START_POSITION = CHARGING_STATION_POSITION;
    public static final int BASE_TICK_MILLIS = 850; // * Simülasyonun temel hız değeri.

    private SimulationConfig() {
    }
}
