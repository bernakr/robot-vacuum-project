package com.robotvacuum.model;

import com.robotvacuum.model.enums.CleaningAlgorithm;
import com.robotvacuum.model.enums.Direction;
import com.robotvacuum.model.enums.SimulationState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Robot {
    private Position position;
    private Direction direction;
    private final Battery battery;
    private SimulationState state;
    private CleaningAlgorithm cleaningAlgorithm;
    private final List<Position> movementPath = new ArrayList<>();
    private final List<Position> returnPath = new ArrayList<>();
    private final Set<Position> visitedPositions = new LinkedHashSet<>();
    private final Set<Position> cleanedPositions = new LinkedHashSet<>();
    private Dirt activeDirt;
    private int spiralLegLength = 1;
    private int spiralLegProgress = 0;
    private int spiralLegsAtLength = 0;
    private boolean wallFollowInitialized;
    private int wallFollowStepsSinceNewCell;

    public Robot(Position position, Direction direction, Battery battery, CleaningAlgorithm cleaningAlgorithm) {
        this.position = position;
        this.direction = direction;
        this.battery = battery;
        this.cleaningAlgorithm = cleaningAlgorithm;
        this.state = SimulationState.READY;
        this.movementPath.add(position);
        this.visitedPositions.add(position);
        this.cleanedPositions.add(position);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Battery getBattery() {
        return battery;
    }

    public SimulationState getState() {
        return state;
    }

    public void setState(SimulationState state) {
        this.state = state;
    }

    public CleaningAlgorithm getCleaningAlgorithm() {
        return cleaningAlgorithm;
    }

    public void setCleaningAlgorithm(CleaningAlgorithm cleaningAlgorithm) {
        this.cleaningAlgorithm = cleaningAlgorithm;
    }

    public List<Position> getMovementPath() {
        return Collections.unmodifiableList(movementPath);
    }

    public void addMovementPosition(Position position) {
        movementPath.add(position);
        visitedPositions.add(position);
    }

    public Set<Position> getVisitedPositions() {
        return Collections.unmodifiableSet(visitedPositions);
    }

    public Set<Position> getCleanedPositions() {
        return Collections.unmodifiableSet(cleanedPositions);
    }

    public void markCleaned(Position position) {
        if (cleanedPositions.add(position)) {
            wallFollowStepsSinceNewCell = 0;
        }
    }

    public List<Position> getReturnPath() {
        return Collections.unmodifiableList(returnPath);
    }

    public void setReturnPath(List<Position> path) {
        returnPath.clear();
        returnPath.addAll(path);
    }

    public void clearReturnPath() {
        returnPath.clear();
    }

    public Dirt getActiveDirt() {
        return activeDirt;
    }

    public void setActiveDirt(Dirt activeDirt) {
        this.activeDirt = activeDirt;
    }

    public int getSpiralLegLength() {
        return spiralLegLength;
    }

    public int getSpiralLegProgress() {
        return spiralLegProgress;
    }

    public void advanceSpiralProgress() {
        spiralLegProgress++;
        if (spiralLegProgress >= spiralLegLength) {
            spiralLegProgress = 0;
            spiralLegsAtLength++;
            direction = direction.turnRight();
            if (spiralLegsAtLength >= 2) {
                spiralLegsAtLength = 0;
                spiralLegLength++;
            }
        }
    }

    public void resetSpiral() {
        spiralLegLength = 1;
        spiralLegProgress = 0;
        spiralLegsAtLength = 0;
    }

    public boolean isWallFollowInitialized() {
        return wallFollowInitialized;
    }

    public void setWallFollowInitialized(boolean wallFollowInitialized) {
        this.wallFollowInitialized = wallFollowInitialized;
        this.wallFollowStepsSinceNewCell = 0;
    }

    public int getWallFollowStepsSinceNewCell() {
        return wallFollowStepsSinceNewCell;
    }

    public void advanceWallFollowStep() {
        wallFollowStepsSinceNewCell++;
    }

    public void resetWallFollow() {
        wallFollowInitialized = false;
        wallFollowStepsSinceNewCell = 0;
    }

    public void resetAlgorithmState() {
        resetSpiral();
        resetWallFollow();
    }
}
