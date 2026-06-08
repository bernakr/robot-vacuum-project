package com.robotvacuum.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.robotvacuum.model.Position;
import com.robotvacuum.model.Robot;
import com.robotvacuum.model.Room;
import com.robotvacuum.model.enums.CleaningAlgorithm;
import com.robotvacuum.model.enums.Direction;

// Robotun seçili temizlik algoritmasına göre bir sonraki hareket yönünü belirleyen servis.
// Bu sınıf robotu doğrudan hareket ettirmez; sadece Direction döndürür.
// Gerçek hareket, bu yön bilgisini kullanan controller/simulation katmanında yapılır.

/*
chooseDirection()
→ Hangi algoritma seçili ona bakar.

chooseRandom()
→ Önce rastgele yönlerde temizlenmemiş hücre arar.
→ Yoksa en yakın temizlenmemiş hücreye yön bulur.
→ O da yoksa geçilebilir herhangi bir yöne gider.

chooseSpiral()
→ Odanın merkezinden başlayarak spiral hedef listesi oluşturur.
→ Temizlenmemiş ve geçilebilir ilk hedefe doğru yön seçer.

chooseWallFollow()
→ Önce gerçek oda sınırına gitmeye çalışır.
→ Sonra duvar/engel/temizlenmiş alan kenarındaki frontier hücreleri takip eder.

chooseDirectionToNearest()
→ Küçük BFS mantığıyla en yakın hedefe giden ilk yönü bulur.

 */
public class CleaningMovementService {
    private final Random random = new Random(214);

    public Optional<Direction> chooseDirection(Room room, Robot robot) {
        CleaningAlgorithm algorithm = robot.getCleaningAlgorithm();
        if (algorithm == CleaningAlgorithm.RANDOM) { // Random temizlik seçimi
            return chooseRandom(room, robot);
        }
        if (algorithm == CleaningAlgorithm.WALL_FOLLOW) {
            return chooseWallFollow(room, robot);
        }
        return chooseSpiral(room, robot);
    }

    private Optional<Direction> chooseRandom(Room room, Robot robot) {
        List<Direction> directions = shuffledDirections();
        return firstUncleanedDirection(room, robot, directions)
                .or(() -> chooseDirectionToNearestUncleaned(room, robot))
                .or(() -> firstValidDirection(room, robot.getPosition(), directions));
    }

    private Optional<Direction> firstUncleanedDirection(Room room, Robot robot, List<Direction> candidateDirections) {
        for (Direction direction : candidateDirections) {
            Position next = robot.getPosition().move(direction);
            if (room.isPassableForCleaning(next) && !robot.getCleanedPositions().contains(next)) {
                return Optional.of(direction);
            }
        }
        return Optional.empty();
    }

    private Optional<Direction> chooseDirectionToNearestUncleaned(Room room, Robot robot) {
        return chooseDirectionToNearest(room, robot, position -> !robot.getCleanedPositions().contains(position));
    }

    private Optional<Direction> chooseDirectionToNearest(Room room, Robot robot, Predicate<Position> targetPredicate) {
        Queue<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        Map<Position, Position> previous = new HashMap<>();
        Position start = robot.getPosition();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (!current.equals(start) && targetPredicate.test(current)) {
                Position firstStep = current;
                while (!previous.get(firstStep).equals(start)) {
                    firstStep = previous.get(firstStep);
                }
                return Optional.of(Direction.fromDelta(firstStep.row() - start.row(), firstStep.col() - start.col()));
            }

            for (Direction direction : Direction.values()) {
                Position next = current.move(direction);
                if (room.isPassableForCleaning(next) && visited.add(next)) {
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Direction> chooseSpiral(Room room, Robot robot) {
        for (Position target : spiralTargets(room)) {
            if (robot.getCleanedPositions().contains(target) || !room.isPassableForCleaning(target)) {
                continue;
            }
            if (target.equals(robot.getPosition())) {
                return firstUncleanedDirection(room, robot, spiralDirections(robot.getDirection()))
                        .or(() -> chooseDirectionToNearestUncleaned(room, robot));
            }
            Optional<Direction> direction = chooseDirectionToNearest(room, robot, target::equals);
            if (direction.isPresent()) {
                return direction;
            }
        }

        return chooseDirectionToNearestUncleaned(room, robot)
                .or(() -> firstValidDirection(room, robot.getPosition(), spiralDirections(robot.getDirection())));
    }

    private List<Position> spiralTargets(Room room) {
        List<Position> targets = new ArrayList<>();
        int centerRow = room.getRows() / 2;
        int centerCol = room.getCols() / 2;
        Position current = new Position(centerRow, centerCol);
        addIfInside(room, targets, current);

        Direction direction = Direction.RIGHT;
        int legLength = 1;
        while (targets.size() < room.getRows() * room.getCols()) {
            for (int leg = 0; leg < 2; leg++) {
                for (int step = 0; step < legLength; step++) {
                    current = current.move(direction);
                    addIfInside(room, targets, current);
                }
                direction = direction.turnRight();
            }
            legLength++;
        }

        return targets;
    }

    private void addIfInside(Room room, List<Position> targets, Position position) {
        if (room.isInside(position)) {
            targets.add(position);
        }
    }

    private Optional<Direction> chooseWallFollow(Room room, Robot robot) {
        if (!robot.isWallFollowInitialized()) {
            Optional<Direction> directionToBoundary = chooseDirectionToNearestRealBoundaryContact(room, robot);
            if (directionToBoundary.isPresent() && !isRealBoundaryContact(room, robot.getPosition())) {
                return directionToBoundary;
            }
            robot.setWallFollowInitialized(true);
        }

        Optional<Direction> localFrontier = firstFrontierDirection(room, robot,
                wallFollowDirections(robot.getDirection()));
        if (localFrontier.isPresent()) {
            robot.setWallFollowInitialized(true);
            robot.advanceWallFollowStep();
            return localFrontier;
        }

        Optional<Direction> directionToFrontier = chooseDirectionToNearestFrontier(room, robot);
        if (directionToFrontier.isPresent()) {
            if (robot.getWallFollowStepsSinceNewCell() > room.getRows() + room.getCols()) {
                robot.resetWallFollow();
            }
            return directionToFrontier;
        }

        for (Direction direction : wallFollowDirections(robot.getDirection())) {
            Position next = robot.getPosition().move(direction);
            if (room.isPassableForCleaning(next)) {
                robot.setWallFollowInitialized(true);
                robot.advanceWallFollowStep();
                return Optional.of(direction);
            }
        }

        robot.resetWallFollow();
        return chooseDirectionToNearestUncleaned(room, robot);
    }

    private Optional<Direction> chooseDirectionToNearestRealBoundaryContact(Room room, Robot robot) {
        Queue<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        Map<Position, Position> previous = new HashMap<>();
        Position start = robot.getPosition();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (!current.equals(start) && isRealBoundaryContact(room, current)) {
                Position firstStep = current;
                while (!previous.get(firstStep).equals(start)) {
                    firstStep = previous.get(firstStep);
                }
                return Optional.of(Direction.fromDelta(firstStep.row() - start.row(), firstStep.col() - start.col()));
            }

            for (Direction direction : Direction.values()) {
                Position next = current.move(direction);
                if (room.isPassableForCleaning(next) && visited.add(next)) {
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Direction> firstFrontierDirection(Room room, Robot robot, List<Direction> candidateDirections) {
        for (Direction direction : candidateDirections) {
            Position next = robot.getPosition().move(direction);
            if (isFrontier(room, robot, next)) {
                return Optional.of(direction);
            }
        }
        return Optional.empty();
    }

    private Optional<Direction> chooseDirectionToNearestFrontier(Room room, Robot robot) {
        Queue<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        Map<Position, Position> previous = new HashMap<>();
        Position start = robot.getPosition();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (!current.equals(start) && isFrontier(room, robot, current)) {
                Position firstStep = current;
                while (!previous.get(firstStep).equals(start)) {
                    firstStep = previous.get(firstStep);
                }
                return Optional.of(Direction.fromDelta(firstStep.row() - start.row(), firstStep.col() - start.col()));
            }

            for (Direction direction : Direction.values()) {
                Position next = current.move(direction);
                if (room.isPassableForCleaning(next) && visited.add(next)) {
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }

        return Optional.empty();
    }

    private boolean isFrontier(Room room, Robot robot, Position position) {
        return room.isPassableForCleaning(position)
                && !robot.getCleanedPositions().contains(position)
                && isWallContact(room, robot, position);
    }

    private boolean isWallContact(Room room, Robot robot, Position position) {
        for (Direction direction : Direction.values()) {
            if (isWallLike(room, robot, position.move(direction))) {
                return true;
            }
        }
        return false;
    }

    private boolean isWallLike(Room room, Robot robot, Position position) {
        if (!room.isInside(position)) {
            return true;
        }
        return !room.isPassableForCleaning(position) || robot.getCleanedPositions().contains(position);
    }

    // Verilen pozisyon oda sınırına temas ediyor mu kontrol eder.
    // Komşu hücrelerden biri grid dışındaysa true döner.
    private boolean isRealBoundaryContact(Room room, Position position) {
        for (Direction direction : Direction.values()) {
            if (!room.isInside(position.move(direction))) {
                return true;
            }
        }
        return false;
    }

    // Verilen yönler arasında robotun geçebileceği ilk uygun yönü bulur.
    // Uygun yön bulunursa döner, yoksa boş Optional döner.
    private Optional<Direction> firstValidDirection(
            Room room,
            Position position,
            List<Direction> candidateDirections) {
        for (Direction direction : candidateDirections) {
            if (room.isPassableForCleaning(position.move(direction))) {
                return Optional.of(direction);
            }
        }
        return Optional.empty();
    }

    // ducar takip üzerinede giderken yöneldnirmesini belrliyor robotun ön yüzüne
    // göre
    // ilerle sonra sola dön ve revverse ile de üste tırmanıyor gibi 3 2 1 yönünde
    // düzelmde gidiyor
    private List<Direction> wallFollowDirections(Direction current) {
        return List.of(
                current.turnRight(),
                current,
                current.turnLeft(),
                current.reverse());
    }

    // spiral için mevcut noktadan sağa gidiyor sonra sola ve tkerar reverse ile
    // yukarı döngü şeklinde alanı büyüterek ilerliyor
    private List<Direction> spiralDirections(Direction current) {
        return List.of(
                current,
                current.turnRight(),
                current.turnLeft(),
                current.reverse());
    }

    // Yönlerinn rastgele olark atnaması için kullandığımız fonksiyon robotoun
    // konumdan sonra harekt yönlenrini belirliyor
    private List<Direction> shuffledDirections() {
        List<Direction> directions = new ArrayList<>(List.of(Direction.values()));
        for (int i = directions.size() - 1; i > 0; i--) {
            int swapIndex = random.nextInt(i + 1);
            Direction current = directions.get(i);
            directions.set(i, directions.get(swapIndex));
            directions.set(swapIndex, current);
        }
        return directions;
    }
}
