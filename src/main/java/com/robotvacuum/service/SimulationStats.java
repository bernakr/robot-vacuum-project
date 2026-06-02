package com.robotvacuum.service;

public record SimulationStats(
        int totalArea,
        int cleanableArea,
        int obstacleArea,
        int remainingDirt,
        int remainingDirtyArea,
        int remainingCleanableArea,
        int visitedCleanableArea,
        double cleanedPercentage,
        double remainingCleanablePercentage,
        double dirtPercentage,
        double collectedDirtPercentage
) {
}
