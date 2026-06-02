package com.robotvacuum.service;

import com.robotvacuum.model.Position;
import com.robotvacuum.model.Robot;
import com.robotvacuum.model.Room;

public class StatisticsService {
    public SimulationStats calculate(Room room, Robot robot) {
        int totalArea = totalArea(room);
        int obstacleArea = obstacleArea(room);
        int cleanableArea = cleanableArea(room);
        int remainingDirt = remainingDirt(room);
        int cleanedArea = cleanedArea(room, robot);
        int remainingCleanableArea = Math.max(0, cleanableArea - cleanedArea);
        double cleanedPercentage = cleanedPercentage(cleanableArea, cleanedArea);
        double remainingCleanablePercentage = remainingCleanablePercentage(cleanableArea, remainingCleanableArea);
        double dirtPercentage = cleanableArea == 0 ? 0.0 : (remainingDirt * 100.0) / cleanableArea;
        double collectedDirtPercentage = collectedDirtPercentage(room);

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

    public int totalArea(Room room) {
        return room.getRows() * room.getCols();
    }

    public int obstacleArea(Room room) {
        return room.getObstacles().stream()
                .mapToInt(obstacle -> obstacle.getPositions().size())
                .sum();
    }

    public int remainingDirt(Room room) {
        return room.getActiveDirt().size();
    }

    public int cleanableArea(Room room) {
        int chargingStationArea = room.getChargingStation() == null ? 0 : 1;
        return totalArea(room) - obstacleArea(room) - chargingStationArea;
    }

    public double cleanedPercentage(Room room) {
        int cleanableArea = cleanableArea(room);
        if (cleanableArea == 0) {
            return 100.0;
        }
        return ((cleanableArea - remainingDirt(room)) * 100.0) / cleanableArea;
    }

    public int cleanedArea(Room room, Robot robot) {
        int cleaned = 0;
        for (Position position : robot.getCleanedPositions()) {
            if (room.isInside(position) && room.isPassableForCleaning(position)) {
                cleaned++;
            }
        }
        return cleaned;
    }

    private double cleanedPercentage(int cleanableArea, int cleanedArea) {
        if (cleanableArea == 0) {
            return 100.0;
        }
        return (cleanedArea * 100.0) / cleanableArea;
    }

    private double remainingCleanablePercentage(int cleanableArea, int remainingCleanableArea) {
        if (cleanableArea == 0) {
            return 0.0;
        }
        return (remainingCleanableArea * 100.0) / cleanableArea;
    }

    private double collectedDirtPercentage(Room room) {
        if (room.getTotalDirtTarget() == 0) {
            return 0.0;
        }
        return (room.getCollectedDirtCount() * 100.0) / room.getTotalDirtTarget();
    }
}
