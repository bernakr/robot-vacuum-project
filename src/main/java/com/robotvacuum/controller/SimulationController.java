package com.robotvacuum.controller;

import com.robotvacuum.config.DemoRoomFactory;
import com.robotvacuum.config.SimulationConfig;
import com.robotvacuum.model.Battery;
import com.robotvacuum.model.Dirt;
import com.robotvacuum.model.FurnitureTemplate;
import com.robotvacuum.model.Position;
import com.robotvacuum.model.Robot;
import com.robotvacuum.model.Room;
import com.robotvacuum.model.enums.CleaningAlgorithm;
import com.robotvacuum.model.enums.Direction;
import com.robotvacuum.model.enums.DirtType;
import com.robotvacuum.model.enums.SimulationState;
import com.robotvacuum.service.BfsPathfindingService;
import com.robotvacuum.service.CleaningMovementService;
import com.robotvacuum.service.StatisticsService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class SimulationController {
    private static final List<FurnitureTemplate> FURNITURE_TEMPLATES = List.of(
            new FurnitureTemplate("Koltuk", 2, 4),
            new FurnitureTemplate("Tekli Koltuk", 2, 2),
            new FurnitureTemplate("Orta Masa", 2, 3),
            new FurnitureTemplate("Orta Masa", 2, 4),
            new FurnitureTemplate("Yemek Masası", 3, 3),
            new FurnitureTemplate("Çalışma Masası", 3, 3),
            new FurnitureTemplate("Konsol", 1, 4),
            new FurnitureTemplate("Konsol", 2, 4),
            new FurnitureTemplate("Dolap", 2, 2),
            new FurnitureTemplate("Dolap", 2, 3),
            new FurnitureTemplate("Kitaplık", 3, 1),
            new FurnitureTemplate("Kitaplık", 3, 2),
            new FurnitureTemplate("Bitki", 1, 1)
    );

    private final BfsPathfindingService pathfindingService = new BfsPathfindingService();
    private final CleaningMovementService movementService = new CleaningMovementService();
    private final StatisticsService statisticsService = new StatisticsService();
    private final GridController gridController = new GridController(this);
    private final Random dirtRandom = new Random(642);
    private final Random furnitureRandom = new Random(913);
    private final Timeline timeline;

    private Room room;
    private Robot robot;
    private Runnable onChange = () -> { };
    private java.util.function.Consumer<String> onMessage = message -> { };
    private int elapsedSeconds;
    private int returnPathIndex;
    private SimulationState stateBeforePause = SimulationState.RUNNING;
    private Position workResumePosition;
    private String lastBatteryEvent = "Hazır";
    private int movementStepsSinceBatteryCost;
    private boolean cleaningCompleted;

    public SimulationController() {
        resetModel();
        timeline = new Timeline(new KeyFrame(Duration.millis(SimulationConfig.BASE_TICK_MILLIS), event -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        if (robot.getState() == SimulationState.READY) {
            clearMessage();
            gridController.setEditMode(GridController.EditMode.DIRT);
            robot.setState(SimulationState.RUNNING);
            timeline.play();
            notifyChange();
        } else if (robot.getState() == SimulationState.PAUSED && stateBeforePause != SimulationState.ERROR) {
            clearMessage();
            gridController.setEditMode(GridController.EditMode.DIRT);
            robot.setState(stateBeforePause == SimulationState.PAUSED ? SimulationState.RUNNING : stateBeforePause);
            timeline.play();
            notifyChange();
        }
    }

    public void pause() {
        if (robot.getState() != SimulationState.READY && robot.getState() != SimulationState.PAUSED) {
            stateBeforePause = robot.getState();
            robot.setState(SimulationState.PAUSED);
            timeline.pause();
            notifyChange();
        }
    }

    public void reset() {
        timeline.stop();
        clearMessage();
        gridController.clearSelectionSilently();
        resetModel();
        notifyChange();
    }

    public void returnToChargingStation() {
        returnToChargingStation(true);
    }

    private void returnToChargingStation(boolean resumeAfterCharging) {
        if (resumeAfterCharging && !robot.getPosition().equals(room.getChargingStation().getPosition())) {
            workResumePosition = robot.getPosition();
        } else if (!resumeAfterCharging) {
            workResumePosition = null;
        }

        List<Position> path = pathfindingService.findPathToChargingStation(room, robot.getPosition());

        if (path.isEmpty()) {
            robot.setState(SimulationState.PAUSED);
            stateBeforePause = SimulationState.ERROR;
            timeline.pause();
            sendMessage("Şarj istasyonuna geçerli BFS yolu bulunamadı.");
            notifyChange();
            return;
        }

        robot.setReturnPath(path);
        returnPathIndex = Math.min(1, path.size());
        robot.setState(SimulationState.RETURNING_TO_CHARGER);
        clearMessage();
        timeline.play();
        notifyChange();
    }

    public void cellSelected(Position position) {
        sendMessage("Seçili hücre: (" + position.row() + ", " + position.col() + "). Kir eklemek için Kir Ekle butonuna bas.");
        notifyChange();
    }

    public void selectionCleared() {
        sendMessage("Hücre seçimi kaldırıldı. Butonlar rastgele uygun alanı kullanacak.");
        notifyChange();
    }

    public void showMessage(String message) {
        sendMessage(message);
        notifyChange();
    }

    public void addDirt(Position position, DirtType type) {
        Optional<String> error = room.addDirt(position, type, robot.getPosition(), robot.getCleanedPositions());
        error.ifPresentOrElse(message -> {
            sendMessage(message);
            notifyChange();
        }, () -> {
            sendMessage(type.displayName() + " eklendi: (" + position.row() + ", " + position.col() + ")");
            notifyChange();
        });
    }

    public void addRandomDirt(DirtType type) {
        List<Position> candidates = new ArrayList<>();
        for (int row = 0; row < room.getRows(); row++) {
            for (int col = 0; col < room.getCols(); col++) {
                Position position = new Position(row, col);
                if (room.validateDirtPlacement(position, robot.getPosition(), robot.getCleanedPositions()).isEmpty()) {
                    candidates.add(position);
                }
            }
        }

        if (candidates.isEmpty()) {
            sendMessage("Kir eklenebilecek temizlenmemiş boş hücre kalmadı.");
            notifyChange();
            return;
        }

        Position position = candidates.get(dirtRandom.nextInt(candidates.size()));
        addDirt(position, type);
    }

    public void addObstacle(Position position) {
        if (robot.getState() == SimulationState.RUNNING
                || robot.getState() == SimulationState.CLEANING
                || robot.getState() == SimulationState.RETURNING_TO_CHARGER
                || robot.getState() == SimulationState.RETURNING_TO_WORK
                || robot.getState() == SimulationState.CHARGING) {
            sendMessage("Simülasyon çalışırken mobilya eklenemez.");
            notifyChange();
            return;
        }

        List<FurnitureTemplate> templates = shuffledFurnitureTemplates();
        for (FurnitureTemplate template : templates) {
            if (placeFurniture(template, position)) {
                return;
            }
        }

        sendMessage("Seçili hücreye uygun mobilya yerleştirilemedi.");
        notifyChange();
    }

    public void addRandomFurniture() {
        if (robot.getState() == SimulationState.RUNNING
                || robot.getState() == SimulationState.CLEANING
                || robot.getState() == SimulationState.RETURNING_TO_CHARGER
                || robot.getState() == SimulationState.RETURNING_TO_WORK
                || robot.getState() == SimulationState.CHARGING) {
            sendMessage("Simülasyon çalışırken mobilya eklenemez.");
            notifyChange();
            return;
        }

        List<FurniturePlacement> placements = new ArrayList<>();
        for (FurnitureTemplate template : FURNITURE_TEMPLATES) {
            for (int row = 0; row < room.getRows(); row++) {
                for (int col = 0; col < room.getCols(); col++) {
                    Position anchor = new Position(row, col);
                    Set<Position> positions = template.positionsAt(anchor);
                    if (room.validateObstaclePlacement(positions, robot.getPosition(), robot.getCleanedPositions()).isEmpty()) {
                        placements.add(new FurniturePlacement(template, anchor));
                    }
                }
            }
        }
        Collections.shuffle(placements, furnitureRandom);

        for (FurniturePlacement placement : placements) {
            if (placeFurniture(placement.template(), placement.anchor())) {
                return;
            }
        }

        sendMessage("Mobilya için uygun boş alan bulunamadı.");
        notifyChange();
    }

    private boolean placeFurniture(FurnitureTemplate template, Position anchor) {
        Set<Position> positions = template.positionsAt(anchor);
        Optional<String> error = room.addObstacle(template.name(), positions, robot.getPosition(), robot.getCleanedPositions());
        if (error.isPresent()) {
            return false;
        }

        List<Position> path = pathfindingService.findPathToChargingStation(room, robot.getPosition());
        if (path.isEmpty()) {
            room.removeObstacleAt(anchor);
            return false;
        }

        sendMessage(template.name() + " eklendi: (" + anchor.row() + ", " + anchor.col() + ")");
        notifyChange();
        return true;
    }

    private List<FurnitureTemplate> shuffledFurnitureTemplates() {
        List<FurnitureTemplate> templates = new ArrayList<>(FURNITURE_TEMPLATES);
        Collections.shuffle(templates, furnitureRandom);
        return templates;
    }

    private record FurniturePlacement(FurnitureTemplate template, Position anchor) {
    }

    public void setBatteryLevel(int level) {
        try {
            robot.getBattery().setLevel(level);
            if (robot.getState() == SimulationState.RUNNING
                    && level <= SimulationConfig.LOW_BATTERY_THRESHOLD
                    && !robot.getPosition().equals(room.getChargingStation().getPosition())) {
                returnToChargingStation();
                return;
            }
            clearMessage();
            notifyChange();
        } catch (IllegalArgumentException exception) {
            sendMessage(exception.getMessage());
        }
    }

    public void setCleaningAlgorithm(CleaningAlgorithm algorithm) {
        robot.setCleaningAlgorithm(algorithm);
        robot.resetAlgorithmState();
        clearMessage();
        notifyChange();
    }

    public void setSpeed(double speed) {
        timeline.setRate(speed);
    }

    public void tick() {
        if (shouldAdvanceElapsedTime()) {
            elapsedSeconds++;
        }
        SimulationState state = robot.getState();
        if (state == SimulationState.CLEANING) {
            tickCleaning();
        } else if (state == SimulationState.RETURNING_TO_CHARGER) {
            tickReturning();
        } else if (state == SimulationState.CHARGING) {
            tickCharging();
        } else if (state == SimulationState.RETURNING_TO_WORK) {
            tickReturningToWork();
        } else if (state == SimulationState.RUNNING) {
            tickRunning();
        }
        notifyChange();
    }

    private boolean shouldAdvanceElapsedTime() {
        return !cleaningCompleted
                || room.getChargingStation() == null
                || !robot.getPosition().equals(room.getChargingStation().getPosition());
    }

    private void tickRunning() {
        if (isCleaningComplete()) {
            completeCleaning();
            return;
        }

        if (robot.getBattery().getLevel() <= SimulationConfig.LOW_BATTERY_THRESHOLD) {
            returnToChargingStation();
            return;
        }

        Dirt currentDirt = room.getCell(robot.getPosition()).getDirt();
        if (currentDirt != null) {
            robot.setActiveDirt(currentDirt);
            robot.setState(SimulationState.CLEANING);
            return;
        }

        Optional<Direction> direction = movementService.chooseDirection(room, robot);
        if (direction.isEmpty()) {
            robot.setState(SimulationState.PAUSED);
            timeline.pause();
            sendMessage("Robot geçerli hareket yönü bulamadı.");
            return;
        }

        moveRobot(direction.get(), true, true);

        Dirt dirtAfterMove = room.getCell(robot.getPosition()).getDirt();
        if (dirtAfterMove != null) {
            robot.setActiveDirt(dirtAfterMove);
            robot.setState(SimulationState.CLEANING);
        }
    }

    private void tickCleaning() {
        Dirt dirt = robot.getActiveDirt();
        if (dirt == null) {
            robot.setState(SimulationState.RUNNING);
            return;
        }

        dirt.cleanOneTick();
        if (dirt.isCleaned()) {
            robot.getBattery().consume(dirt.getType().batteryCost());
            lastBatteryEvent = String.format("%s temizliği -%.0f", dirt.getType().displayName(), dirt.getType().batteryCost());
            room.cleanDirt(robot.getPosition());
            robot.markCleaned(robot.getPosition());
            robot.setActiveDirt(null);
            if (robot.getBattery().getLevel() <= SimulationConfig.LOW_BATTERY_THRESHOLD) {
                returnToChargingStation();
            } else if (isCleaningComplete()) {
                completeCleaning();
            } else {
                robot.setState(SimulationState.RUNNING);
            }
        }
    }

    private void tickReturning() {
        List<Position> path = robot.getReturnPath();
        if (returnPathIndex >= path.size()) {
            robot.setState(SimulationState.CHARGING);
            robot.clearReturnPath();
            return;
        }

        Position next = path.get(returnPathIndex);
        Direction direction = Direction.fromDelta(
                next.row() - robot.getPosition().row(),
                next.col() - robot.getPosition().col()
        );
        moveRobot(direction, false, false);
        returnPathIndex++;

        if (robot.getPosition().equals(room.getChargingStation().getPosition())) {
            robot.setState(SimulationState.CHARGING);
            robot.clearReturnPath();
        }
    }

    private void tickCharging() {
        robot.getBattery().charge(SimulationConfig.CHARGE_PER_TICK);
        lastBatteryEvent = String.format("Şarj +%.0f", SimulationConfig.CHARGE_PER_TICK);
        if (robot.getBattery().isFull() && cleaningCompleted) {
            robot.setState(SimulationState.PAUSED);
            sendMessage("Temizlik tamamlandı. Robot şarj istasyonunda.");
        } else if (robot.getBattery().isFull()) {
            startReturnToWork();
        }
    }

    private void startReturnToWork() {
        if (workResumePosition == null || workResumePosition.equals(robot.getPosition())) {
            workResumePosition = null;
            robot.setState(SimulationState.RUNNING);
            return;
        }

        List<Position> path = pathfindingService.findPath(room, robot.getPosition(), workResumePosition);
        if (path.isEmpty()) {
            workResumePosition = null;
            robot.setState(SimulationState.RUNNING);
            sendMessage("Eski konuma dönüş yolu bulunamadı; robot şarj istasyonundan devam edecek.");
            return;
        }

        robot.setReturnPath(path);
        returnPathIndex = Math.min(1, path.size());
        robot.setState(SimulationState.RETURNING_TO_WORK);
        clearMessage();
    }

    private void tickReturningToWork() {
        List<Position> path = robot.getReturnPath();
        if (returnPathIndex >= path.size()) {
            finishReturnToWork();
            return;
        }

        Position next = path.get(returnPathIndex);
        Direction direction = Direction.fromDelta(
                next.row() - robot.getPosition().row(),
                next.col() - robot.getPosition().col()
        );
        moveRobot(direction, false, false);
        returnPathIndex++;

        if (robot.getPosition().equals(workResumePosition)) {
            finishReturnToWork();
        }
    }

    private void finishReturnToWork() {
        workResumePosition = null;
        robot.clearReturnPath();
        robot.setState(SimulationState.RUNNING);
    }

    private void moveRobot(Direction direction, boolean clearReturnPath, boolean countsAsCleaning) {
        Position next = robot.getPosition().move(direction);
        if (!isRobotMovementAllowed(next, countsAsCleaning)) {
            robot.setDirection(direction.turnRight());
            return;
        }
        robot.setDirection(direction);
        robot.setPosition(next);
        robot.addMovementPosition(next);
        if (countsAsCleaning && robot.getCleaningAlgorithm() == CleaningAlgorithm.SPIRAL) {
            robot.advanceSpiralProgress();
        }
        if (countsAsCleaning && !room.getCell(next).hasDirt()) {
            robot.markCleaned(next);
        }
        consumeMovementBattery();
        if (clearReturnPath) {
            robot.clearReturnPath();
        }
    }

    private boolean isRobotMovementAllowed(Position next, boolean countsAsCleaning) {
        if (countsAsCleaning) {
            return room.isPassableForCleaning(next);
        }
        Position allowedStationGoal = robot.getState() == SimulationState.RETURNING_TO_CHARGER
                ? room.getChargingStation().getPosition()
                : null;
        return room.isPassableForPath(next, allowedStationGoal);
    }

    private void consumeMovementBattery() {
        movementStepsSinceBatteryCost++;
        if (movementStepsSinceBatteryCost >= SimulationConfig.MOVES_PER_BATTERY_COST) {
            robot.getBattery().consume(SimulationConfig.MOVEMENT_BATTERY_COST);
            movementStepsSinceBatteryCost = 0;
            lastBatteryEvent = String.format("Hareket -%.0f", SimulationConfig.MOVEMENT_BATTERY_COST);
        } else {
            lastBatteryEvent = "Hareket 1/2";
        }
    }

    private boolean isCleaningComplete() {
        return !cleaningCompleted
                && room.getActiveDirt().isEmpty()
                && statisticsService.calculate(room, robot).visitedCleanableArea() >= statisticsService.cleanableArea(room);
    }

    private void completeCleaning() {
        cleaningCompleted = true;
        workResumePosition = null;
        returnToChargingStation(false);
        sendMessage("Temizlik tamamlandı. Robot şarj istasyonuna dönüyor.");
    }

    private void resetModel() {
        room = DemoRoomFactory.createRoom();
        robot = new Robot(
                SimulationConfig.ROBOT_START_POSITION,
                Direction.RIGHT,
                new Battery(SimulationConfig.INITIAL_BATTERY),
                CleaningAlgorithm.SPIRAL
        );
        elapsedSeconds = 0;
        returnPathIndex = 0;
        stateBeforePause = SimulationState.RUNNING;
        workResumePosition = null;
        lastBatteryEvent = "Hazır";
        movementStepsSinceBatteryCost = 0;
        cleaningCompleted = false;
    }

    public Room getRoom() {
        return room;
    }

    public Robot getRobot() {
        return robot;
    }

    public GridController getGridController() {
        return gridController;
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public String getLastBatteryEvent() {
        return lastBatteryEvent;
    }

    public boolean shouldShowCompletionOverlay() {
        return cleaningCompleted && robot.getPosition().equals(room.getChargingStation().getPosition());
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> { } : onChange;
    }

    public void setOnMessage(java.util.function.Consumer<String> onMessage) {
        this.onMessage = onMessage == null ? message -> { } : onMessage;
    }

    private void notifyChange() {
        onChange.run();
    }

    private void sendMessage(String message) {
        onMessage.accept(message);
    }

    private void clearMessage() {
        onMessage.accept("");
    }
}
