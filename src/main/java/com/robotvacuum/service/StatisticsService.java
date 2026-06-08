package com.robotvacuum.service;

import com.robotvacuum.model.Position;
import com.robotvacuum.model.Robot;
import com.robotvacuum.model.Room;

public class StatisticsService {

    // Oda ve robot verilerinden tüm simülasyon istatistiklerini hesaplar
    public SimulationStats calculate(Room room, Robot robot) {

        // Odanın toplam kare alanını hesaplar
        int totalArea = totalArea(room);

        // Engellerin kapladığı toplam alanı hesaplar
        int obstacleArea = obstacleArea(room);

        // Robotun temizleyebileceği toplam alanı hesaplar
        int cleanableArea = cleanableArea(room);

        // Yerde kalan kir sayısını hesaplar
        int remainingDirt = remainingDirt(room);

        // Robotun temizlediği toplam alanı hesaplar
        int cleanedArea = cleanedArea(room, robot);

        // Henüz temizlenmemiş alan miktarını hesaplar
        int remainingCleanableArea = Math.max(0, cleanableArea - cleanedArea);

        // Temizlenen alanın yüzdesini hesaplar
        double cleanedPercentage = cleanedPercentage(cleanableArea, cleanedArea);

        // Temizlenmemiş alanın yüzdesini hesaplar
        double remainingCleanablePercentage =
                remainingCleanablePercentage(cleanableArea, remainingCleanableArea);

        // Kalan kirin temizlenebilir alana oranını hesaplar
        double dirtPercentage =
                cleanableArea == 0 ? 0.0 : (remainingDirt * 100.0) / cleanableArea;

        // Toplanan kir yüzdesini hesaplar
        double collectedDirtPercentage = collectedDirtPercentage(room);

        // Hesaplanan tüm istatistikleri tek nesnede toplar
        return new SimulationStats(
                totalArea,
                cleanableArea,
                obstacleArea,
                remainingDirt,
                remainingDirt,
                remainingCleanableArea,
                cleanedArea,
                cleanedPercentage,
                remainingCleanablePercentage,
                dirtPercentage,
                collectedDirtPercentage
        );
    }

    // Odanın toplam alanını hesaplar
    public int totalArea(Room room) {
        return room.getRows() * room.getCols();
    }

    // Tüm engellerin kapladığı toplam kare alanını hesaplar
    public int obstacleArea(Room room) {
        return room.getObstacles().stream()

                // Her engelin kapladığı kare sayısını alır
                .mapToInt(obstacle -> obstacle.getPositions().size())

                // Tüm engel alanlarını toplar
                .sum();
    }

    // Odada kalan aktif kir sayısını döndürür
    public int remainingDirt(Room room) {
        return room.getActiveDirt().size();
    }

    // Robotun temizleyebileceği toplam alanı hesaplar
    public int cleanableArea(Room room) {

        // Şarj istasyonu varsa 1 kare alan kaplar
        int chargingStationArea =
                room.getChargingStation() == null ? 0 : 1;

        // Toplam alandan engel ve şarj alanını çıkarır
        return totalArea(room)
                - obstacleArea(room)
                - chargingStationArea;
    }

    // Temizlenen alan yüzdesini hesaplar
    public double cleanedPercentage(Room room) {

        // Temizlenebilir alanı hesaplar
        int cleanableArea = cleanableArea(room);

        // Bölme hatasını önlemek için kontrol yapar
        if (cleanableArea == 0) {
            return 100.0;
        }

        // Temizlenen alan yüzdesini döndürür
        return ((cleanableArea - remainingDirt(room)) * 100.0)
                / cleanableArea;
    }

    // Robotun temizlediği toplam kare sayısını hesaplar
    public int cleanedArea(Room room, Robot robot) {

        // Temizlenen kare sayısını tutar
        int cleaned = 0;

        // Robotun temizlediği tüm pozisyonları dolaşır
        for (Position position : robot.getCleanedPositions()) {

            // Pozisyon oda içinde ve temizlenebilir mi kontrol eder
            if (room.isInside(position)
                    && room.isPassableForCleaning(position)) {

                // Şart sağlanıyorsa temizlenen alanı artırır
                cleaned++;
            }
        }

        // Toplam temizlenen alanı döndürür
        return cleaned;
    }

    // Temizlenen alanın yüzde oranını hesaplar
    private double cleanedPercentage(
            int cleanableArea,
            int cleanedArea
    ) {

        // Bölme hatasını önlemek için kontrol yapar
        if (cleanableArea == 0) {
            return 100.0;
        }

        // Temizlenen alan yüzdesini döndürür
        return (cleanedArea * 100.0) / cleanableArea;
    }

    // Kalan temizlenebilir alanın yüzde oranını hesaplar
    private double remainingCleanablePercentage(
            int cleanableArea,
            int remainingCleanableArea
    ) {

        // Bölme hatasını önlemek için kontrol yapar
        if (cleanableArea == 0) {
            return 0.0;
        }

        // Kalan alan yüzdesini döndürür
        return (remainingCleanableArea * 100.0)
                / cleanableArea;
    }

    // Toplanan kir yüzdesini hesaplar
    private double collectedDirtPercentage(Room room) {

        // Başlangıçta hiç kir yoksa hata oluşmasını engeller
        if (room.getTotalDirtTarget() == 0) {
            return 0.0;
        }

        // Toplanan kir oranını yüzdelik olarak döndürür
        return (room.getCollectedDirtCount() * 100.0)
                / room.getTotalDirtTarget();
    }
}