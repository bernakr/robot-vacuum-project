package com.robotvacuum.service;

import com.robotvacuum.model.Position;
import com.robotvacuum.model.Room;
import com.robotvacuum.model.enums.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BfsPathfindingService {
    public List<Position> findPath(Room room, Position start, Position goal) {
        return findPath(room, start, goal, null);
    }

    public List<Position> findPathToChargingStation(Room room, Position start) {
        return findPath(room, start, room.getChargingStation().getPosition(), room.getChargingStation().getPosition());
    }

    private List<Position> findPath(Room room, Position start, Position goal, Position allowedChargingStationGoal) {
        if (!room.isPassableForPath(start, start) || !room.isPassableForPath(goal, allowedChargingStationGoal)) {
            return List.of();
        }

        Queue<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        Map<Position, Position> previous = new HashMap<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(goal)) {
                return reconstructPath(previous, start, goal);
            }

            for (Direction direction : Direction.values()) {
                Position next = current.move(direction);
                if (room.isPassableForPath(next, allowedChargingStationGoal) && visited.add(next)) {
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }

        return List.of();
    }

    private List<Position> reconstructPath(Map<Position, Position> previous, Position start, Position goal) {
        List<Position> path = new ArrayList<>();
        Position current = goal;
        path.add(current);

        while (!current.equals(start)) {
            current = previous.get(current);
            if (current == null) {
                return List.of();
            }
            path.add(current);
        }

        Collections.reverse(path);
        return path;
    }
}
