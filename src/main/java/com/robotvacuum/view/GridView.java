package com.robotvacuum.view;

import com.robotvacuum.controller.GridController;
import com.robotvacuum.model.Cell;
import com.robotvacuum.model.Dirt;
import com.robotvacuum.model.Obstacle;
import com.robotvacuum.model.Position;
import com.robotvacuum.model.Robot;
import com.robotvacuum.model.Room;
import com.robotvacuum.model.enums.DirtType;
import com.robotvacuum.model.enums.SimulationState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GridView extends Pane {
    private enum DragKind {
        NONE,
        FURNITURE,
        DIRT
    }

    private static final double COORD_TOP_SPACE = 22;
    private static final double COORD_LEFT_SPACE = 30;
    private static final double PAD = 12;
    private static final double WALL_MIN = 24;
    private static final double WALL_MAX = 40;
    private static final double WALL_RATIO = 0.5;
    private static final double DRAG_THRESHOLD = 5;

    private final Canvas canvas = new Canvas();
    private final GridController gridController;
    private Room room;
    private Robot robot;
    private boolean showCompletionOverlay;
    private double cellSize;
    private double gridX;
    private double gridY;
    private DragKind dragKind = DragKind.NONE;
    private Position dragSourcePosition;
    private Position dragPreviewPosition;
    private double dragStartX;
    private double dragStartY;
    private boolean dragActive;
    private boolean suppressNextClick;

    public GridView(GridController gridController) {
        this.gridController = gridController;
        getStyleClass().add("grid-shell");
        getChildren().add(canvas);

        widthProperty().addListener((observable, oldValue, newValue) -> resizeCanvas());
        heightProperty().addListener((observable, oldValue, newValue) -> resizeCanvas());

        canvas.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY || room == null) {
                return;
            }
            Position position = toPosition(event.getX(), event.getY());
            dragKind = dragKindAt(position);
            dragSourcePosition = dragKind == DragKind.NONE ? null : position;
            dragPreviewPosition = null;
            dragStartX = event.getX();
            dragStartY = event.getY();
            dragActive = false;
        });

        canvas.setOnMouseDragged(event -> {
            if (dragKind == DragKind.NONE || dragSourcePosition == null) {
                return;
            }
            double dx = event.getX() - dragStartX;
            double dy = event.getY() - dragStartY;
            if (!dragActive && Math.hypot(dx, dy) < DRAG_THRESHOLD) {
                return;
            }
            dragActive = true;
            dragPreviewPosition = toPosition(event.getX(), event.getY());
            draw();
            event.consume();
        });

        canvas.setOnMouseReleased(event -> {
            if (event.getButton() != MouseButton.PRIMARY || !dragActive) {
                resetDragState(false);
                return;
            }
            Position target = toPosition(event.getX(), event.getY());
            if (target != null && dragSourcePosition != null) {
                if (dragKind == DragKind.FURNITURE) {
                    gridController.moveFurniture(dragSourcePosition, target);
                } else if (dragKind == DragKind.DIRT) {
                    gridController.moveDirt(dragSourcePosition, target);
                }
            }
            resetDragState(true);
            draw();
            event.consume();
        });

        canvas.setOnMouseClicked(event -> {
            if (suppressNextClick) {
                suppressNextClick = false;
                return;
            }
            if (event.getButton() != MouseButton.PRIMARY || room == null) {
                return;
            }
            Position position = toPosition(event.getX(), event.getY());
            if (position != null) {
                gridController.handleCellClick(position);
            } else {
                gridController.clearSelection();
            }
        });
    }

    public void render(Room room, Robot robot, boolean showCompletionOverlay) {
        this.room = room;
        this.robot = robot;
        this.showCompletionOverlay = showCompletionOverlay;
        draw();
    }

    private void resizeCanvas() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        draw();
    }

    private Position toPosition(double x, double y) {
        double roomWidth = room.getCols() * cellSize;
        double roomHeight = room.getRows() * cellSize;
        if (x < gridX || x >= gridX + roomWidth || y < gridY || y >= gridY + roomHeight) {
            return null;
        }
        int col = (int) ((x - gridX) / cellSize);
        int row = (int) ((y - gridY) / cellSize);
        Position position = new Position(row, col);
        return room.isInside(position) ? position : null;
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (room == null || robot == null || canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }

        calculateGridGeometry();

        drawRoomBase(gc);
        drawVisitedCells(gc);
        drawCoordinates(gc);
        drawPaths(gc);
        drawObstacles(gc);
        drawDirt(gc);
        drawChargingStation(gc);
        drawRobot(gc);
        drawSelectedCell(gc);
        drawDragPreview(gc);
        if (showCompletionOverlay) {
            drawCompletionOverlay(gc);
        }
    }

    private DragKind dragKindAt(Position position) {
        if (position == null || !isDragEditingAllowed()) {
            return DragKind.NONE;
        }
        Cell cell = room.getCell(position);
        if (cell.hasObstacle()) {
            return DragKind.FURNITURE;
        }
        if (cell.hasDirt()) {
            return DragKind.DIRT;
        }
        return DragKind.NONE;
    }

    private boolean isDragEditingAllowed() {
        return robot != null
                && (robot.getState() == SimulationState.READY || robot.getState() == SimulationState.PAUSED);
    }

    private void resetDragState(boolean suppressClick) {
        dragKind = DragKind.NONE;
        dragSourcePosition = null;
        dragPreviewPosition = null;
        dragActive = false;
        suppressNextClick = suppressClick;
    }

    private void calculateGridGeometry() {
        double availableWidth = Math.max(1, canvas.getWidth() - PAD * 2 - COORD_LEFT_SPACE);
        double availableHeight = Math.max(1, canvas.getHeight() - PAD * 2 - COORD_TOP_SPACE);
        double estimatedCellSize = Math.min(
                availableWidth / (room.getCols() + WALL_RATIO * 2),
                availableHeight / (room.getRows() + WALL_RATIO * 2)
        );

        double wall = wallThickness(estimatedCellSize);
        for (int i = 0; i < 3; i++) {
            cellSize = Math.min(
                    (availableWidth - wall * 2) / room.getCols(),
                    (availableHeight - wall * 2) / room.getRows()
            );
            wall = wallThickness(cellSize);
        }

        double roomWidth = cellSize * room.getCols();
        double roomHeight = cellSize * room.getRows();
        double totalWidth = COORD_LEFT_SPACE + wall + roomWidth + wall;
        double totalHeight = COORD_TOP_SPACE + wall + roomHeight + wall;
        double extraHorizontalSpace = Math.max(0, canvas.getWidth() - PAD * 2 - totalWidth);
        double extraVerticalSpace = Math.max(0, canvas.getHeight() - PAD * 2 - totalHeight);

        gridX = PAD + extraHorizontalSpace / 2 + COORD_LEFT_SPACE + wall;
        gridY = PAD + extraVerticalSpace / 2 + COORD_TOP_SPACE + wall;
    }

    private void drawRoomBase(GraphicsContext gc) {
        double width = cellSize * room.getCols();
        double height = cellSize * room.getRows();

        gc.setFill(Color.web("#8d6740"));
        gc.fillRoundRect(gridX - 8, gridY - 8, width + 16, height + 16, 12, 12);
        gc.setFill(Color.web("#5f4328"));
        gc.fillRoundRect(gridX - 4, gridY - 4, width + 8, height + 8, 9, 9);

        for (int row = 0; row < room.getRows(); row++) {
            for (int col = 0; col < room.getCols(); col++) {
                double x = gridX + col * cellSize;
                double y = gridY + row * cellSize;
                Color base = row % 2 == 0 ? Color.web("#c89458") : Color.web("#bd864f");
                gc.setFill(col % 2 == 0 ? base : base.brighter().deriveColor(0, 0.98, 1.03, 1));
                gc.fillRect(x, y, cellSize, cellSize);
                gc.setStroke(Color.web("#6f512f", 0.42));
                gc.setLineWidth(1);
                gc.strokeRect(x, y, cellSize, cellSize);
            }
        }
        drawOuterWalls(gc, width, height);
    }

    private void drawOuterWalls(GraphicsContext gc, double roomWidth, double roomHeight) {
        double wall = wallThickness(cellSize);
        double doorWidth = cellSize * 2.4;
        double doorX = gridX + roomWidth / 2 - doorWidth / 2;
        double bottomY = gridY + roomHeight;
        double left = gridX - wall;
        double top = gridY - wall;
        double right = gridX + roomWidth;

        gc.setFill(Color.web("#050607", 0.44));
        gc.fillRoundRect(left - wall * 0.22, top - wall * 0.18,
                roomWidth + wall * 2.44, roomHeight + wall * 2.36, 8, 8);

        gc.setFill(Color.web("#1a1b1d"));
        gc.fillRect(left, top, roomWidth + wall * 2, wall);
        gc.fillRect(left, top, wall, roomHeight + wall * 2);
        gc.fillRect(right, top, wall, roomHeight + wall * 2);
        gc.fillRect(left, bottomY, doorX - gridX + wall, wall);
        gc.fillRect(doorX + doorWidth, bottomY, gridX + roomWidth + wall - (doorX + doorWidth), wall);

        gc.setFill(Color.web("#333437"));
        gc.fillRect(left, top, roomWidth + wall * 2, wall * 0.24);
        gc.fillRect(left, top, wall * 0.24, roomHeight + wall * 2);
        gc.fillRect(right + wall * 0.76, top, wall * 0.24, roomHeight + wall * 2);

        gc.setFill(Color.web("#0b0c0e"));
        gc.fillPolygon(
                new double[]{left, left + wall, left + wall, left},
                new double[]{top + wall, top, top + wall * 0.22, top + wall * 1.22},
                4
        );
        gc.setStroke(Color.web("#424448", 0.65));
        gc.setLineWidth(Math.max(1.8, wall * 0.08));
        gc.strokeLine(left + wall * 0.12, top + wall * 0.96, left + wall * 0.96, top + wall * 0.12);

        gc.setStroke(Color.web("#050608"));
        gc.setLineWidth(Math.max(2.0, wall * 0.14));
        gc.strokeRect(left, top, roomWidth + wall * 2, roomHeight + wall * 2);

        gc.setStroke(Color.web("#5e6065", 0.54));
        gc.setLineWidth(Math.max(1.2, wall * 0.08));
        gc.strokeLine(gridX, gridY, gridX + roomWidth, gridY);
        gc.strokeLine(gridX, gridY, gridX, gridY + roomHeight);
        gc.strokeLine(gridX + roomWidth, gridY, gridX + roomWidth, gridY + roomHeight);
        gc.strokeLine(gridX, bottomY, doorX, bottomY);
        gc.strokeLine(doorX + doorWidth, bottomY, gridX + roomWidth, bottomY);

        gc.setFill(Color.web("#c9aa83"));
        gc.fillRect(doorX, bottomY, doorWidth, wall * 0.7);
        gc.setStroke(Color.web("#7a5b3e", 0.72));
        gc.setLineWidth(1.6);
        gc.strokeLine(doorX, bottomY + wall * 0.7, doorX + doorWidth, bottomY + wall * 0.7);
        gc.strokeLine(doorX + doorWidth / 2, bottomY, doorX + doorWidth / 2, bottomY + wall * 0.7);
    }

    private void drawCoordinates(GraphicsContext gc) {
        double wall = wallThickness(cellSize);
        gc.setFill(Color.web("#d7dde8"));
        gc.setFont(Font.font("System", FontWeight.BOLD, Math.max(11, Math.min(15, cellSize * 0.27))));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int col = 0; col < room.getCols(); col++) {
            gc.fillText(String.valueOf(col), centerX(col), gridY - wall - 7);
        }
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int row = 0; row < room.getRows(); row++) {
            gc.fillText(String.valueOf(row), gridX - wall - 9, centerY(row) + 4);
        }
    }

    private double wallThickness(double size) {
        return Math.min(WALL_MAX, Math.max(WALL_MIN, size * WALL_RATIO));
    }

    private void drawVisitedCells(GraphicsContext gc) {
        gc.setFill(Color.web("#f4f0dc", 0.22));
        for (Position position : robot.getCleanedPositions()) {
            if (room.isInside(position) && room.isPassableForCleaning(position)) {
                double x = gridX + position.col() * cellSize + 2;
                double y = gridY + position.row() * cellSize + 2;
                gc.fillRoundRect(x, y, cellSize - 4, cellSize - 4, 5, 5);
            }
        }
    }

    private void drawSelectedCell(GraphicsContext gc) {
        Position selectedPosition = gridController.getSelectedPosition();
        if (selectedPosition == null || !room.isInside(selectedPosition)) {
            return;
        }
        double x = gridX + selectedPosition.col() * cellSize + 3;
        double y = gridY + selectedPosition.row() * cellSize + 3;
        gc.setLineDashes(null);
        gc.setStroke(Color.web("#ffcf4d"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(x, y, cellSize - 6, cellSize - 6, 6, 6);
    }

    private void drawDragPreview(GraphicsContext gc) {
        if (!dragActive || dragPreviewPosition == null || dragSourcePosition == null) {
            return;
        }

        gc.setLineDashes(6, 5);
        gc.setLineWidth(3);
        gc.setStroke(Color.web("#ffcf4d", 0.92));
        gc.setFill(Color.web("#ffcf4d", 0.16));

        if (dragKind == DragKind.DIRT) {
            drawPreviewCell(gc, dragPreviewPosition);
        } else if (dragKind == DragKind.FURNITURE) {
            room.findObstacleAt(dragSourcePosition).ifPresent(obstacle -> {
                int rowDelta = dragPreviewPosition.row() - dragSourcePosition.row();
                int colDelta = dragPreviewPosition.col() - dragSourcePosition.col();
                Set<Position> previewPositions = new LinkedHashSet<>();
                for (Position position : obstacle.getPositions()) {
                    previewPositions.add(new Position(position.row() + rowDelta, position.col() + colDelta));
                }
                for (Position position : previewPositions) {
                    if (room.isInside(position)) {
                        drawPreviewCell(gc, position);
                    }
                }
            });
        }
        gc.setLineDashes(null);
    }

    private void drawPreviewCell(GraphicsContext gc, Position position) {
        double x = gridX + position.col() * cellSize + 4;
        double y = gridY + position.row() * cellSize + 4;
        gc.fillRoundRect(x, y, cellSize - 8, cellSize - 8, 6, 6);
        gc.strokeRoundRect(x, y, cellSize - 8, cellSize - 8, 6, 6);
    }

    private void drawPaths(GraphicsContext gc) {
        List<Position> movementPath = robot.getMovementPath();
        gc.setLineWidth(2.2);
        gc.setStroke(Color.web("#2f7dff", 0.78));
        gc.setFill(Color.web("#2f9dff", 0.92));
        gc.setLineDashes(7, 6);
        drawPath(gc, movementPath, true, true);

        gc.setLineDashes(null);
        gc.setLineWidth(3.2);
        gc.setStroke(Color.web("#24d7c6", 0.9));
        gc.setFill(Color.web("#20e578", 0.92));
        drawPath(gc, robot.getReturnPath(), true, false);
    }

    private void drawPath(GraphicsContext gc, List<Position> path, boolean drawArrows, boolean dashed) {
        if (path.size() < 2) {
            return;
        }
        for (int i = 1; i < path.size(); i++) {
            Position from = path.get(i - 1);
            Position to = path.get(i);
            gc.strokeLine(centerX(from.col()), centerY(from.row()), centerX(to.col()), centerY(to.row()));
            if (drawArrows && i % 4 == 0) {
                drawPathArrow(gc, from, to, dashed);
            }
        }
    }

    private void drawPathArrow(GraphicsContext gc, Position from, Position to, boolean dashed) {
        double startX = centerX(from.col());
        double startY = centerY(from.row());
        double endX = centerX(to.col());
        double endY = centerY(to.row());
        double angle = Math.atan2(endY - startY, endX - startX);
        double arrowLength = Math.max(7, cellSize * 0.22);
        double arrowWidth = arrowLength * 0.68;
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;

        double backX = midX - Math.cos(angle) * arrowLength;
        double backY = midY - Math.sin(angle) * arrowLength;
        double leftX = backX + Math.cos(angle + Math.PI / 2) * arrowWidth;
        double leftY = backY + Math.sin(angle + Math.PI / 2) * arrowWidth;
        double rightX = backX + Math.cos(angle - Math.PI / 2) * arrowWidth;
        double rightY = backY + Math.sin(angle - Math.PI / 2) * arrowWidth;
        gc.setLineDashes(null);
        gc.fillPolygon(new double[]{midX, leftX, rightX}, new double[]{midY, leftY, rightY}, 3);
        if (dashed) {
            gc.setLineDashes(7, 6);
        }
    }

    private void drawObstacles(GraphicsContext gc) {
        for (Obstacle obstacle : room.getObstacles()) {
            int minRow = obstacle.getPositions().stream().map(Position::row).min(Comparator.naturalOrder()).orElse(0);
            int maxRow = obstacle.getPositions().stream().map(Position::row).max(Comparator.naturalOrder()).orElse(0);
            int minCol = obstacle.getPositions().stream().map(Position::col).min(Comparator.naturalOrder()).orElse(0);
            int maxCol = obstacle.getPositions().stream().map(Position::col).max(Comparator.naturalOrder()).orElse(0);
            double x = gridX + minCol * cellSize + 3;
            double y = gridY + minRow * cellSize + 3;
            double width = (maxCol - minCol + 1) * cellSize - 6;
            double height = (maxRow - minRow + 1) * cellSize - 6;

            drawFurniture(gc, obstacle.getName(), x, y, width, height, obstacle.getRotationDegrees());
        }
    }

    private void drawFurniture(
            GraphicsContext gc,
            String name,
            double x,
            double y,
            double width,
            double height,
            int rotationDegrees
    ) {
        if (rotationDegrees == 0) {
            drawFurniture(gc, name, x, y, width, height);
            return;
        }

        boolean sideways = Math.floorMod(rotationDegrees, 180) != 0;
        double drawWidth = sideways ? height : width;
        double drawHeight = sideways ? width : height;
        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(rotationDegrees);
        drawFurniture(gc, name, -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight);
        gc.restore();
    }

    private void drawFurniture(GraphicsContext gc, String name, double x, double y, double width, double height) {
        Image image = ImageAssets.furniture(name);
        if (image != null) {
            drawAsset(gc, image, x, y, width, height, name.contains("Bitki") ? 1.12 : 1.0);
            return;
        }

        if (name.contains("Koltuk") && !name.contains("Tekli")) {
            drawSofa(gc, x, y, width, height);
        } else if (name.contains("Orta Masa")) {
            drawCoffeeTable(gc, x, y, width, height);
        } else if (name.contains("Tekli")) {
            drawArmchair(gc, x, y, width, height);
        } else if (name.contains("Yemek") || name.contains("Çalışma")) {
            drawDesk(gc, x, y, width, height);
        } else if (name.contains("Konsol")) {
            drawConsole(gc, x, y, width, height);
        } else if (name.contains("Dolap")) {
            drawClosetFallback(gc, x, y, width, height);
        } else if (name.contains("Kitaplık")) {
            drawBookcase(gc, x, y, width, height);
        } else if (name.contains("Bitki")) {
            drawPlant(gc, x, y, width, height);
        } else {
            drawGenericFurniture(gc, x, y, width, height);
        }
    }

    private void drawSofa(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.12);
        double bodyX = x + width * 0.08;
        double bodyY = y + height * 0.18;
        double bodyW = width * 0.84;
        double bodyH = height * 0.68;
        gc.setFill(Color.web("#d7b892"));
        gc.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 14, 14);
        gc.setFill(Color.web("#e3c8a4"));
        gc.fillRoundRect(bodyX + bodyW * 0.06, bodyY + bodyH * 0.08, bodyW * 0.43, bodyH * 0.78, 10, 10);
        gc.fillRoundRect(bodyX + bodyW * 0.51, bodyY + bodyH * 0.08, bodyW * 0.43, bodyH * 0.78, 10, 10);
        gc.setStroke(Color.web("#8a684c", 0.55));
        gc.setLineWidth(1.3);
        gc.strokeLine(bodyX + bodyW * 0.5, bodyY + bodyH * 0.1, bodyX + bodyW * 0.5, bodyY + bodyH * 0.86);
        gc.setFill(Color.web("#c59d73"));
        gc.fillRoundRect(x + width * 0.02, y + height * 0.2, width * 0.12, height * 0.64, 10, 10);
        gc.fillRoundRect(x + width * 0.86, y + height * 0.2, width * 0.12, height * 0.64, 10, 10);
        gc.fillRoundRect(bodyX, y + height * 0.08, bodyW, height * 0.2, 11, 11);
        drawPillow(gc, x + width * 0.27, y + height * 0.36, width * 0.16, height * 0.22);
        drawPillow(gc, x + width * 0.61, y + height * 0.36, width * 0.16, height * 0.22);
        gc.setStroke(Color.web("#6d4d38", 0.72));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(bodyX, bodyY, bodyW, bodyH, 14, 14);
    }

    private void drawCoffeeTable(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.12);
        gc.setFill(Color.web("#b9a48c", 0.52));
        gc.fillRoundRect(x + width * 0.02, y + height * 0.02, width * 0.96, height * 0.96, 8, 8);
        double topX = x + width * 0.18;
        double topY = y + height * 0.24;
        double topW = width * 0.64;
        double topH = height * 0.52;
        drawWoodRect(gc, topX, topY, topW, topH, 9);
        drawLegs(gc, topX, topY, topW, topH);
        drawSmallPlant(gc, x + width * 0.48, y + height * 0.43, Math.min(width, height) * 0.18);
    }

    private void drawArmchair(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.15);
        gc.setFill(Color.web("#d2ae86"));
        gc.fillRoundRect(x + width * 0.18, y + height * 0.08, width * 0.64, height * 0.84, 13, 13);
        gc.setFill(Color.web("#e4c49c"));
        gc.fillRoundRect(x + width * 0.28, y + height * 0.22, width * 0.44, height * 0.56, 10, 10);
        gc.setFill(Color.web("#bb8f67"));
        gc.fillRoundRect(x + width * 0.08, y + height * 0.24, width * 0.2, height * 0.54, 9, 9);
        gc.fillRoundRect(x + width * 0.72, y + height * 0.24, width * 0.2, height * 0.54, 9, 9);
        gc.fillRoundRect(x + width * 0.23, y + height * 0.06, width * 0.54, height * 0.22, 10, 10);
        gc.setStroke(Color.web("#6d4d38", 0.75));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x + width * 0.18, y + height * 0.08, width * 0.64, height * 0.84, 13, 13);
    }

    private void drawDesk(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.12);
        double tableX = x + width * 0.13;
        double tableY = y + height * 0.1;
        double tableW = width * 0.68;
        double tableH = height * 0.76;
        drawWoodRect(gc, tableX, tableY, tableW, tableH, 8);
        drawLegs(gc, tableX, tableY, tableW, tableH);
        gc.setFill(Color.web("#6f7c52"));
        gc.fillRoundRect(x + width * 0.78, y + height * 0.26, width * 0.16, height * 0.5, 8, 8);
        gc.setStroke(Color.web("#5a351e"));
        gc.setLineWidth(2);
        gc.strokeLine(x + width * 0.82, y + height * 0.2, x + width * 0.82, y + height * 0.82);
        drawSmallPlant(gc, x + width * 0.5, y + height * 0.45, Math.min(width, height) * 0.16);
    }

    private void drawConsole(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.12);
        drawWoodRect(gc, x + width * 0.06, y + height * 0.14, width * 0.88, height * 0.72, 7);
        gc.setStroke(Color.web("#5a2f17", 0.75));
        gc.setLineWidth(1.5);
        gc.strokeLine(x + width * 0.12, y + height * 0.62, x + width * 0.88, y + height * 0.62);
        gc.strokeLine(x + width * 0.34, y + height * 0.62, x + width * 0.34, y + height * 0.82);
        gc.strokeLine(x + width * 0.66, y + height * 0.62, x + width * 0.66, y + height * 0.82);
        gc.setFill(Color.web("#47321f"));
        gc.fillOval(x + width * 0.46, y + height * 0.7, 3.5, 3.5);
        gc.fillOval(x + width * 0.53, y + height * 0.7, 3.5, 3.5);
        drawSmallPlant(gc, x + width * 0.22, y + height * 0.3, Math.min(width, height) * 0.18);
        drawBooks(gc, x + width * 0.72, y + height * 0.22, width * 0.14, height * 0.36);
    }

    private void drawBookcase(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.08);
        double shelfX = x + width * 0.16;
        double shelfY = y + height * 0.04;
        double shelfW = width * 0.68;
        double shelfH = height * 0.92;

        gc.setFill(Color.web("#633719"));
        gc.fillRoundRect(shelfX, shelfY, shelfW, shelfH, 5, 5);
        gc.setFill(Color.web("#8f4d1f"));
        gc.fillRoundRect(shelfX + shelfW * 0.08, shelfY + shelfH * 0.04,
                shelfW * 0.84, shelfH * 0.9, 4, 4);
        gc.setStroke(Color.web("#3e2110"));
        gc.setLineWidth(Math.max(1.2, Math.min(width, height) * 0.04));
        gc.strokeRoundRect(shelfX, shelfY, shelfW, shelfH, 5, 5);

        int shelfCount = 4;
        double innerX = shelfX + shelfW * 0.15;
        double innerW = shelfW * 0.7;
        for (int i = 1; i < shelfCount; i++) {
            double shelfLineY = shelfY + shelfH * i / shelfCount;
            gc.setStroke(Color.web("#4f2a13", 0.9));
            gc.setLineWidth(1.5);
            gc.strokeLine(shelfX + shelfW * 0.1, shelfLineY, shelfX + shelfW * 0.9, shelfLineY);
        }

        Color[] colors = {
                Color.web("#2b90d9"),
                Color.web("#f2d36b"),
                Color.web("#d9534f"),
                Color.web("#7cc36a"),
                Color.web("#efe8d1")
        };
        for (int shelf = 0; shelf < shelfCount; shelf++) {
            double slotTop = shelfY + shelfH * shelf / shelfCount + shelfH * 0.05;
            double slotHeight = shelfH / shelfCount * 0.58;
            double bookW = innerW / 6.5;
            for (int book = 0; book < 5; book++) {
                double bookX = innerX + book * bookW * 1.12;
                double bookH = slotHeight * (book % 2 == 0 ? 1.0 : 0.78);
                double bookY = slotTop + (slotHeight - bookH);
                gc.setFill(colors[(shelf + book) % colors.length]);
                gc.fillRoundRect(bookX, bookY, bookW, bookH, 1.5, 1.5);
                gc.setStroke(Color.web("#2b190f", 0.48));
                gc.setLineWidth(0.7);
                gc.strokeRoundRect(bookX, bookY, bookW, bookH, 1.5, 1.5);
            }
        }
    }

    private void drawClosetFallback(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.12);
        drawWoodRect(gc, x + width * 0.12, y + height * 0.04, width * 0.76, height * 0.92, 5);
        gc.setFill(Color.web("#3b2415", 0.72));
        gc.fillRoundRect(x + width * 0.18, y + height * 0.1, width * 0.1, height * 0.8, 4, 4);
        gc.setStroke(Color.web("#5a2f17", 0.78));
        gc.setLineWidth(1.4);
        gc.strokeLine(x + width * 0.15, y + height * 0.2, x + width * 0.84, y + height * 0.2);
        gc.strokeLine(x + width * 0.15, y + height * 0.5, x + width * 0.84, y + height * 0.5);
        gc.strokeLine(x + width * 0.15, y + height * 0.78, x + width * 0.84, y + height * 0.78);
        drawBooks(gc, x + width * 0.42, y + height * 0.18, width * 0.35, height * 0.18);
        drawBooks(gc, x + width * 0.36, y + height * 0.54, width * 0.4, height * 0.18);
    }

    private void drawPlant(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.08);
        double size = Math.min(width, height) * 0.44;
        drawSmallPlant(gc, x + width / 2, y + height / 2, size);
    }

    private void drawGenericFurniture(GraphicsContext gc, double x, double y, double width, double height) {
        drawFurnitureShadow(gc, x, y, width, height, 0.1);
        drawWoodRect(gc, x + width * 0.08, y + height * 0.08, width * 0.84, height * 0.84, 7);
    }

    private void drawFurnitureShadow(GraphicsContext gc, double x, double y, double width, double height, double insetRatio) {
        double inset = Math.min(width, height) * insetRatio;
        gc.setFill(Color.web("#2b1d14", 0.24));
        gc.fillRoundRect(x + inset * 0.55, y + inset * 0.85, width - inset, height - inset, 12, 12);
    }

    private void drawWoodRect(GraphicsContext gc, double x, double y, double width, double height, double radius) {
        gc.setFill(Color.web("#8c4d22"));
        gc.fillRoundRect(x, y, width, height, radius, radius);
        gc.setFill(Color.web("#a65f2a"));
        gc.fillRoundRect(x + width * 0.04, y + height * 0.05, width * 0.92, height * 0.88, radius * 0.75, radius * 0.75);
        gc.setStroke(Color.web("#5a2f17"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, radius, radius);
        drawWoodGrain(gc, x, y, width, height);
    }

    private void drawWoodGrain(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setStroke(Color.web("#6e3517", 0.24));
        gc.setLineWidth(1);
        int lineCount = Math.max(3, (int) (height / 12));
        for (int i = 1; i <= lineCount; i++) {
            double yy = y + height * i / (lineCount + 1.0);
            gc.strokeLine(x + width * 0.08, yy, x + width * 0.92, yy + Math.sin(i) * 1.5);
        }
        gc.setStroke(Color.web("#c6813f", 0.18));
        for (int i = 1; i <= 3; i++) {
            double xx = x + width * i / 4.0;
            gc.strokeLine(xx, y + height * 0.08, xx + Math.cos(i) * 1.4, y + height * 0.9);
        }
    }

    private void drawLegs(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(Color.web("#4b2b16"));
        double legW = Math.max(3, width * 0.06);
        double legH = Math.max(4, height * 0.14);
        gc.fillRoundRect(x + width * 0.06, y + height * 0.82, legW, legH, 3, 3);
        gc.fillRoundRect(x + width * 0.88, y + height * 0.82, legW, legH, 3, 3);
        gc.fillRoundRect(x + width * 0.06, y + height * 0.05, legW, legH, 3, 3);
        gc.fillRoundRect(x + width * 0.88, y + height * 0.05, legW, legH, 3, 3);
    }

    private void drawPillow(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(Color.web("#6f8a45"));
        gc.fillRoundRect(x, y, width, height, 7, 7);
        gc.setStroke(Color.web("#3f5c2b", 0.8));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(x, y, width, height, 7, 7);
        gc.setFill(Color.web("#9fb36b", 0.35));
        gc.fillRoundRect(x + width * 0.12, y + height * 0.12, width * 0.5, height * 0.28, 5, 5);
    }

    private void drawSmallPlant(GraphicsContext gc, double centerX, double centerY, double size) {
        double potW = size * 0.7;
        double potH = size * 0.46;
        gc.setFill(Color.web("#7d4120"));
        gc.fillOval(centerX - potW / 2, centerY + size * 0.12, potW, potH);
        gc.setStroke(Color.web("#4d2511", 0.75));
        gc.setLineWidth(1.2);
        gc.strokeOval(centerX - potW / 2, centerY + size * 0.12, potW, potH);

        Color leafDark = Color.web("#237a2d");
        Color leafLight = Color.web("#43a843");
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            double leafX = centerX + Math.cos(angle) * size * 0.28;
            double leafY = centerY + Math.sin(angle) * size * 0.23;
            gc.setFill(i % 2 == 0 ? leafLight : leafDark);
            gc.fillOval(leafX - size * 0.18, leafY - size * 0.09, size * 0.36, size * 0.18);
        }
        gc.setFill(Color.web("#2f8d35"));
        gc.fillOval(centerX - size * 0.13, centerY - size * 0.12, size * 0.26, size * 0.26);
    }

    private void drawBooks(GraphicsContext gc, double x, double y, double width, double height) {
        double bookWidth = width / 4.6;
        Color[] colors = {
                Color.web("#2c86d1"),
                Color.web("#e8d8aa"),
                Color.web("#d0483c"),
                Color.web("#6aa7d8")
        };
        for (int i = 0; i < colors.length; i++) {
            double bookX = x + i * bookWidth * 1.08;
            gc.setFill(colors[i]);
            gc.fillRoundRect(bookX, y, bookWidth, height, 2, 2);
            gc.setStroke(Color.web("#3a2516", 0.45));
            gc.setLineWidth(0.8);
            gc.strokeRoundRect(bookX, y, bookWidth, height, 2, 2);
        }
    }

    private void drawFurnitureLabel(GraphicsContext gc, String text, double x, double y, double width, double height) {
        if (width <= 36 || height <= 18) {
            return;
        }
        gc.setFill(Color.web("#eef3fb", 0.78));
        gc.setFont(Font.font("System", FontWeight.BOLD, Math.min(11, cellSize * 0.26)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x + width / 2, y + height / 2 + 4);
    }

    private void drawDirt(GraphicsContext gc) {
        for (int row = 0; row < room.getRows(); row++) {
            for (int col = 0; col < room.getCols(); col++) {
                Cell cell = room.getCell(new Position(row, col));
                Dirt dirt = cell.getDirt();
                if (dirt != null) {
                    drawDirtSymbol(gc, row, col, dirt);
                }
            }
        }
    }

    private void drawDirtSymbol(GraphicsContext gc, int row, int col, Dirt dirt) {
        double x = centerX(col);
        double y = centerY(row);
        double radius = cellSize * 0.22;

        if (dirt.getType() == DirtType.DUST) {
            gc.setStroke(Color.web("#372d22", 0.72));
            gc.setLineWidth(1.2);
            gc.setFill(Color.web("#6b5b4a", 0.8));
            gc.fillOval(x - radius, y - radius, radius * 0.9, radius * 0.9);
            gc.fillOval(x + radius * 0.1, y - radius * 0.6, radius * 0.8, radius * 0.8);
            gc.fillOval(x - radius * 0.2, y + radius * 0.1, radius * 0.95, radius * 0.95);
        } else if (dirt.getType() == DirtType.LIQUID) {
            gc.setStroke(Color.web("#0b75a8", 0.75));
            gc.setLineWidth(1.2);
            gc.setFill(Color.web("#1fa6e8", 0.86));
            gc.fillOval(x - radius, y - radius * 0.7, radius * 2, radius * 1.5);
            gc.strokeOval(x - radius, y - radius * 0.7, radius * 2, radius * 1.5);
            gc.setFill(Color.web("#bdefff", 0.75));
            gc.fillOval(x - radius * 0.35, y - radius * 0.35, radius * 0.45, radius * 0.35);
        } else {
            gc.setStroke(Color.web("#612323", 0.85));
            gc.setLineWidth(1.4);
            gc.setFill(Color.web("#7e2b2b", 0.9));
            gc.fillOval(x - radius * 1.15, y - radius * 0.75, radius * 2.2, radius * 1.5);
            gc.strokeOval(x - radius * 1.15, y - radius * 0.75, radius * 2.2, radius * 1.5);
            gc.setFill(Color.web("#512020", 0.6));
            gc.fillOval(x - radius * 0.5, y - radius * 0.2, radius, radius * 0.55);
        }
    }

    private void drawChargingStation(GraphicsContext gc) {
        Position station = room.getChargingStation().getPosition();
        Image image = ImageAssets.load("charging_station.png");
        if (image != null) {
            double x = gridX + station.col() * cellSize + cellSize * 0.06;
            double y = gridY + station.row() * cellSize + cellSize * 0.06;
            drawAsset(gc, image, x, y, cellSize * 0.88, cellSize * 0.88, 1.0);
            return;
        }

        double x = gridX + station.col() * cellSize + cellSize * 0.12;
        double y = gridY + station.row() * cellSize + cellSize * 0.18;
        double width = cellSize * 0.76;
        double height = cellSize * 0.64;

        gc.setFill(Color.web("#182130"));
        gc.fillRoundRect(x, y, width, height, 7, 7);
        gc.setFill(Color.web("#243348"));
        gc.fillRoundRect(x + width * 0.12, y + height * 0.18, width * 0.76, height * 0.34, 5, 5);
        gc.setStroke(Color.web("#2de0c3"));
        gc.setLineWidth(2.2);
        gc.strokeRoundRect(x, y, width, height, 7, 7);
        gc.setFill(Color.web("#2de0c3"));
        gc.fillRect(x + width * 0.28, y + height * 0.72, width * 0.44, height * 0.12);
    }

    private void drawRobot(GraphicsContext gc) {
        Position position = robot.getPosition();
        double x = centerX(position.col());
        double y = centerY(position.row());
        double radius = cellSize * 0.38;
        Image image = ImageAssets.load("robot_vacuum.png");
        if (image != null) {
            double size = cellSize * 0.96;
            gc.drawImage(image, x - size / 2, y - size / 2, size, size);
            gc.setStroke(Color.web("#ffce45"));
            gc.setLineWidth(Math.max(2, cellSize * 0.05));
            gc.strokeLine(x, y, x + robot.getDirection().colDelta() * cellSize * 0.34,
                    y + robot.getDirection().rowDelta() * cellSize * 0.34);
            drawRobotBatteryBadge(gc, x, y, cellSize * 0.48);
            return;
        }

        gc.setFill(Color.web("#e8edf6"));
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setFill(Color.web("#cfd7e6"));
        gc.fillOval(x - radius * 0.72, y - radius * 0.72, radius * 1.44, radius * 1.44);
        gc.setStroke(Color.web("#151b27"));
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setFill(Color.web("#26364f"));
        gc.fillOval(x - radius * 0.45, y - radius * 0.45, radius * 0.9, radius * 0.9);
        gc.setStroke(Color.web("#ffce45"));
        gc.strokeLine(x, y, x + robot.getDirection().colDelta() * radius * 0.9, y + robot.getDirection().rowDelta() * radius * 0.9);
        drawRobotBatteryBadge(gc, x, y, radius);
    }

    private void drawRobotBatteryBadge(GraphicsContext gc, double robotX, double robotY, double robotRadius) {
        double level = robot.getBattery().getLevel();
        double badgeWidth = Math.max(42, cellSize * 0.94);
        double badgeHeight = Math.max(26, cellSize * 0.48);
        double badgeX = robotX + robotRadius * 0.5;
        double badgeY = robotY - robotRadius * 1.55 - badgeHeight * 0.45;

        double minX = gridX + 3;
        double maxX = gridX + room.getCols() * cellSize - badgeWidth - 3;
        double minY = gridY + 3;
        double maxY = gridY + room.getRows() * cellSize - badgeHeight - 3;
        badgeX = Math.max(minX, Math.min(maxX, badgeX));
        badgeY = Math.max(minY, Math.min(maxY, badgeY));

        Color fillColor = batteryColor(level);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("System", FontWeight.BLACK, Math.max(11, cellSize * 0.23)));
        gc.setFill(Color.web("#101010"));
        gc.fillText(String.format("%.0f%%", level), badgeX + badgeWidth / 2, badgeY + badgeHeight * 0.38);
        gc.setStroke(Color.web("#fff1c6", 0.72));
        gc.setLineWidth(0.8);
        gc.strokeText(String.format("%.0f%%", level), badgeX + badgeWidth / 2, badgeY + badgeHeight * 0.38);

        double barX = badgeX + badgeWidth * 0.18;
        double barY = badgeY + badgeHeight * 0.54;
        double barW = badgeWidth * 0.58;
        double barH = badgeHeight * 0.28;
        gc.setFill(Color.web("#122438"));
        gc.fillRoundRect(barX, barY, barW, barH, 4, 4);
        gc.setStroke(Color.web("#f3e7c5"));
        gc.setLineWidth(1.3);
        gc.strokeRoundRect(barX, barY, barW, barH, 4, 4);
        gc.strokeRoundRect(barX + barW, barY + barH * 0.28, badgeWidth * 0.07, barH * 0.44, 2, 2);

        double fillW = Math.max(2, barW * level / 100.0);
        gc.setFill(fillColor);
        gc.fillRoundRect(barX + 2, barY + 2, Math.max(1, fillW - 4), Math.max(1, barH - 4), 3, 3);
    }

    private Color batteryColor(double level) {
        if (level <= 20) {
            return Color.web("#f0445d");
        }
        if (level <= 50) {
            return Color.web("#ffb21f");
        }
        return Color.web("#28e47b");
    }

    private void drawAsset(GraphicsContext gc, Image image, double x, double y, double width, double height, double scale) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        double fit = Math.min(width / imageWidth, height / imageHeight) * scale;
        double drawWidth = imageWidth * fit;
        double drawHeight = imageHeight * fit;
        double drawX = x + (width - drawWidth) / 2;
        double drawY = y + (height - drawHeight) / 2;
        gc.drawImage(image, drawX, drawY, drawWidth, drawHeight);
    }

    private void drawCompletionOverlay(GraphicsContext gc) {
        double centerX = gridX + room.getCols() * cellSize / 2;
        double centerY = gridY + room.getRows() * cellSize / 2;
        double titleSize = Math.max(34, Math.min(72, cellSize * 1.18));
        double subtitleSize = Math.max(34, Math.min(76, cellSize * 1.22));
        double badgeRadius = Math.max(38, Math.min(66, cellSize * 1.05));

        gc.save();
        gc.setFill(Color.web("#06111f", 0.34));
        gc.fillRoundRect(gridX + cellSize * 1.2, centerY - cellSize * 2.6,
                cellSize * (room.getCols() - 2.4), cellSize * 5.4, 34, 34);

        DropShadow blueShadow = new DropShadow(26, Color.web("#16a7ff"));
        gc.setEffect(blueShadow);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("System", FontWeight.BLACK, titleSize));
        gc.setFill(Color.web("#f7fbff"));
        gc.fillText("TEMİZLİK", centerX, centerY - badgeRadius * 1.35);
        gc.setStroke(Color.web("#0b7edb"));
        gc.setLineWidth(2.4);
        gc.strokeText("TEMİZLİK", centerX, centerY - badgeRadius * 1.35);

        DropShadow greenShadow = new DropShadow(30, Color.web("#52ff00"));
        gc.setEffect(greenShadow);
        gc.setFont(Font.font("System", FontWeight.BLACK, subtitleSize));
        gc.setFill(Color.web("#63ff17"));
        gc.fillText("TAMAMLANDI!", centerX, centerY - badgeRadius * 0.42);
        gc.setStroke(Color.web("#0f7415"));
        gc.setLineWidth(2.6);
        gc.strokeText("TAMAMLANDI!", centerX, centerY - badgeRadius * 0.42);

        gc.setEffect(new DropShadow(34, Color.web("#43ff00")));
        gc.setFill(Color.web("#38ef00"));
        gc.fillOval(centerX - badgeRadius, centerY + badgeRadius * 0.05,
                badgeRadius * 2, badgeRadius * 2);
        gc.setStroke(Color.web("#c6ffbd"));
        gc.setLineWidth(3);
        gc.strokeOval(centerX - badgeRadius, centerY + badgeRadius * 0.05,
                badgeRadius * 2, badgeRadius * 2);

        gc.setEffect(null);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(Math.max(8, badgeRadius * 0.16));
        gc.strokeLine(centerX - badgeRadius * 0.42, centerY + badgeRadius * 1.05,
                centerX - badgeRadius * 0.12, centerY + badgeRadius * 1.36);
        gc.strokeLine(centerX - badgeRadius * 0.12, centerY + badgeRadius * 1.36,
                centerX + badgeRadius * 0.52, centerY + badgeRadius * 0.72);
        gc.restore();
    }

    private double centerX(int col) {
        return gridX + col * cellSize + cellSize / 2;
    }

    private double centerY(int row) {
        return gridY + row * cellSize + cellSize / 2;
    }
}
