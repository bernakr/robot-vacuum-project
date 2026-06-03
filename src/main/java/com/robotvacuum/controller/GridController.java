package com.robotvacuum.controller;

import com.robotvacuum.model.Position;
import com.robotvacuum.model.enums.DirtType;

public class GridController {
    public enum EditMode {
        DIRT,
        OBSTACLE
    }

    private final SimulationController simulationController;
    private EditMode editMode = EditMode.DIRT;
    private DirtType selectedDirtType = DirtType.DUST;
    private Position selectedPosition;

    public GridController(SimulationController simulationController) {
        this.simulationController = simulationController;
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public DirtType getSelectedDirtType() {
        return selectedDirtType;
    }

    public void setSelectedDirtType(DirtType selectedDirtType) {
        this.selectedDirtType = selectedDirtType;
    }

    public Position getSelectedPosition() {
        return selectedPosition;
    }

    public boolean hasSelection() {
        return selectedPosition != null;
    }

    public void clearSelection() {
        selectedPosition = null;
        simulationController.selectionCleared();
    }

    public void clearSelectionSilently() {
        selectedPosition = null;
    }

    public void handleCellClick(Position position) {
        if (editMode == EditMode.OBSTACLE && simulationController.rotateFurnitureAt(position)) {
            selectedPosition = null;
            return;
        }
        if (position.equals(selectedPosition)) {
            clearSelection();
            return;
        }
        selectedPosition = position;
        simulationController.cellSelected(position);
    }

    public void addDirtToSelectedCell() {
        setEditMode(EditMode.DIRT);
        if (selectedPosition == null) {
            simulationController.addRandomDirt(selectedDirtType);
            return;
        }
        Position target = selectedPosition;
        selectedPosition = null;
        simulationController.addDirt(target, selectedDirtType);
    }

    public void addObstacleToSelectedCell() {
        setEditMode(EditMode.OBSTACLE);
        if (selectedPosition == null) {
            simulationController.addRandomFurniture();
            return;
        }
        Position target = selectedPosition;
        selectedPosition = null;
        simulationController.addObstacle(target);
    }

    public boolean moveFurniture(Position source, Position target) {
        selectedPosition = null;
        return simulationController.moveFurniture(source, target);
    }

    public boolean moveDirt(Position source, Position target) {
        selectedPosition = null;
        return simulationController.moveDirt(source, target);
    }
}
