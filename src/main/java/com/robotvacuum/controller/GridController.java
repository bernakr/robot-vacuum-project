package com.robotvacuum.controller;

import com.robotvacuum.model.Position;
import com.robotvacuum.model.enums.DirtType;

//  * Grid üzerindeki kullanıcı işlemlerini yönetir.
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

    // * Aktif düzenleme modunu döndürür.
    public EditMode getEditMode() {
        return editMode;
    }

    // * Düzenleme modunu değiştirir.
    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    // Şu an seçili olan kir tipini getiriyor.
    public DirtType getSelectedDirtType() {
        return selectedDirtType;
    }

    // Seçili kir tipini değiştirme işlemi
    public void setSelectedDirtType(DirtType selectedDirtType) {
        this.selectedDirtType = selectedDirtType;
    }

    // Şu an seçili hücrenin pozisyonunu getiriyor.
    public Position getSelectedPosition() {
        return selectedPosition;
    }

    // Şu an gridde seçilmiş bir hücre var mı? alan kontrolü
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

    // Gridde bir hücreye tıklanınca çalışır.
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

    // kir ekleme 
    public void addDirtToSelectedCell() {
        // mod kontrolü yapıp alanın boş olmasına göre ekleme yapar
        setEditMode(EditMode.DIRT);
        if (selectedPosition == null) {
            simulationController.addRandomDirt(selectedDirtType);
            return;
        }
        Position target = selectedPosition;
        selectedPosition = null;
        simulationController.addDirt(target, selectedDirtType);
    }

    // nesene ekleme 
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

    // Mobilyayı bir hücreden başka hücreye taşır.
    public boolean moveFurniture(Position source, Position target) {
        selectedPosition = null;
        return simulationController.moveFurniture(source, target);
    }

    // kiri taşır
    public boolean moveDirt(Position source, Position target) {
        selectedPosition = null;
        return simulationController.moveDirt(source, target);
    }
}
