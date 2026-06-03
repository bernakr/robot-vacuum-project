package com.robotvacuum.view;

import com.robotvacuum.controller.GridController;
import com.robotvacuum.controller.SimulationController;
import com.robotvacuum.model.FurnitureTemplate;
import com.robotvacuum.model.Robot;
import com.robotvacuum.model.enums.CleaningAlgorithm;
import com.robotvacuum.model.enums.DirtType;
import com.robotvacuum.model.enums.SimulationState;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ControlPanelView extends VBox {
    private final SimulationController controller;
    private final Label speedValue = new Label("1.0x");
    private final Label positionValue = new Label("-");
    private final Label directionValue = new Label("-");
    private final Label batteryValue = new Label("-");
    private final Label stateValue = new Label("-");
    private final Label batteryEventValue = new Label("-");
    private final Label selectedDirtValue = new Label("Toz");
    private final ProgressBar batteryBar = new ProgressBar(1.0);
    private final TextField batteryInput = new TextField();
    private final ToggleButton obstacleButton = new ToggleButton("Mobilya Ekle");
    private final MenuButton furnitureMenuButton = new MenuButton();
    private ToggleButton dirtButton;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button returnHomeButton;

    public ControlPanelView(SimulationController controller) {
        this.controller = controller;
        getStyleClass().add("control-panel");
        setSpacing(8);
        setPadding(new Insets(12));
        setPrefWidth(292);
        setMinWidth(280);
        build();
    }

    private void build() {
        directionValue.setGraphic(IconFactory.create(IconFactory.Icon.ARROW_RIGHT));
        directionValue.getStyleClass().add("direction-value");
        batteryValue.getStyleClass().add("battery-percent");

        dirtButton = new ToggleButton("Kir Ekle");
        ToggleGroup toolGroup = new ToggleGroup();
        dirtButton.setToggleGroup(toolGroup);
        obstacleButton.setToggleGroup(toolGroup);
        dirtButton.setSelected(true);
        dirtButton.setMaxWidth(Double.MAX_VALUE);
        obstacleButton.setMaxWidth(Double.MAX_VALUE);
        styleGraphicButton(dirtButton, IconFactory.Icon.DUST, "tool-primary");
        styleGraphicButton(obstacleButton, IconFactory.Icon.SOFA, "tool-success");
        buildFurnitureMenu();
        dirtButton.setOnAction(event -> {
            controller.getGridController().setEditMode(GridController.EditMode.DIRT);
            controller.getGridController().addDirtToSelectedCell();
        });
        obstacleButton.setOnAction(event -> {
            controller.getGridController().setEditMode(GridController.EditMode.OBSTACLE);
            controller.getGridController().addObstacleToSelectedCell();
        });
        HBox furnitureControl = new HBox(0, obstacleButton, furnitureMenuButton);
        furnitureControl.getStyleClass().add("furniture-control");
        HBox.setHgrow(obstacleButton, Priority.ALWAYS);

        ToggleGroup dirtGroup = new ToggleGroup();
        RadioButton dust = dirtRadio("Toz", DirtType.DUST, IconFactory.Icon.DUST, dirtGroup);
        RadioButton liquid = dirtRadio("Sıvı", DirtType.LIQUID, IconFactory.Icon.LIQUID, dirtGroup);
        RadioButton stain = dirtRadio("Leke", DirtType.STAIN, IconFactory.Icon.STAIN, dirtGroup);
        dust.setSelected(true);
        controller.getGridController().setSelectedDirtType(DirtType.DUST);
        HBox dirtSegment = new HBox(4, dust, liquid, stain);
        dirtSegment.getStyleClass().add("segmented");
        HBox.setHgrow(dust, Priority.ALWAYS);
        HBox.setHgrow(liquid, Priority.ALWAYS);
        HBox.setHgrow(stain, Priority.ALWAYS);

        Slider speedSlider = new Slider(0.25, 2.5, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.75);
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double speed = Math.round(newValue.doubleValue() * 10.0) / 10.0;
            controller.setSpeed(speed);
            speedValue.setText(String.format("%.1fx", speed));
        });
        HBox speedBox = new HBox(10, speedSlider, speedValue);
        speedBox.getStyleClass().add("speed-row");
        HBox.setHgrow(speedSlider, Priority.ALWAYS);

        ToggleGroup algorithmGroup = new ToggleGroup();
        RadioButton randomAlgorithm = algorithmRadio("Rastgele", CleaningAlgorithm.RANDOM, algorithmGroup);
        RadioButton spiralAlgorithm = algorithmRadio("Spiral", CleaningAlgorithm.SPIRAL, algorithmGroup);
        RadioButton wallAlgorithm = algorithmRadio("Duvar Takip", CleaningAlgorithm.WALL_FOLLOW, algorithmGroup);
        spiralAlgorithm.setSelected(true);
        VBox algorithmBox = new VBox(5, randomAlgorithm, spiralAlgorithm, wallAlgorithm);
        algorithmBox.getStyleClass().add("algorithm-list");

        batteryInput.setPromptText("0-100");
        Button applyBattery = new Button("Uygula");
        applyBattery.setOnAction(event -> applyBatteryValue());
        HBox batteryBox = new HBox(8, batteryInput, applyBattery);
        HBox.setHgrow(batteryInput, Priority.ALWAYS);

        startButton = new Button("Başlat");
        pauseButton = new Button("Duraklat");
        resetButton = new Button("Sıfırla");
        returnHomeButton = new Button("İstasyona Dön");
        startButton.setMaxWidth(Double.MAX_VALUE);
        pauseButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setMaxWidth(Double.MAX_VALUE);
        returnHomeButton.setMaxWidth(Double.MAX_VALUE);
        styleGraphicButton(startButton, IconFactory.Icon.PLAY, "control-start");
        styleGraphicButton(pauseButton, IconFactory.Icon.PAUSE, "control-pause");
        styleGraphicButton(resetButton, IconFactory.Icon.STOP, "control-reset");
        styleGraphicButton(returnHomeButton, IconFactory.Icon.HOME, "control-home");
        startButton.setOnAction(event -> controller.start());
        pauseButton.setOnAction(event -> controller.pause());
        resetButton.setOnAction(event -> controller.reset());
        returnHomeButton.setOnAction(event -> controller.returnToChargingStation());
        HBox primaryControls = new HBox(7, startButton, pauseButton);
        HBox.setHgrow(startButton, Priority.ALWAYS);
        HBox.setHgrow(pauseButton, Priority.ALWAYS);

        batteryBar.setMaxWidth(Double.MAX_VALUE);
        batteryBar.setPrefHeight(16);
        batteryBar.getStyleClass().add("battery-bar");

        getChildren().addAll(
                title(),
                section("Araçlar", IconFactory.Icon.TOOLS, dirtButton, statusRow("Kir Türü", selectedDirtValue), dirtSegment, furnitureControl),
                section("Robot Hızı", IconFactory.Icon.SPEED, speedBox),
                section("Temizlik Algoritması", IconFactory.Icon.GEAR, algorithmBox),
                section("Robot Durumu", IconFactory.Icon.ROBOT, statusRow("Konum", positionValue), statusRow("Yön", directionValue),
                        statusRow("Batarya", batteryValue), batteryBar, statusRow("Son Tüketim", batteryEventValue),
                        statusRow("Durum", stateValue), batteryBox),
                section("Kontroller", IconFactory.Icon.GAMEPAD, primaryControls, resetButton, returnHomeButton),
                footer()
        );
    }

    private Label title() {
        Label label = new Label("Robot Süpürge");
        label.getStyleClass().add("side-title");
        return label;
    }

    private VBox section(String title, IconFactory.Icon icon, javafx.scene.Node... children) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        HBox header = new HBox(7, IconFactory.create(icon), label);
        header.getStyleClass().add("section-header");
        VBox box = new VBox(6);
        box.getStyleClass().add("section");
        box.getChildren().add(header);
        box.getChildren().addAll(children);
        return box;
    }

    private RadioButton dirtRadio(String text, DirtType type, IconFactory.Icon icon, ToggleGroup group) {
        RadioButton button = new RadioButton(text);
        button.setToggleGroup(group);
        button.setGraphic(IconFactory.create(icon));
        button.getStyleClass().add("segment-radio");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            controller.getGridController().setSelectedDirtType(type);
            controller.getGridController().setEditMode(GridController.EditMode.DIRT);
            selectedDirtValue.setText(type.displayName());
            dirtButton.setSelected(true);
        });
        return button;
    }

    private void buildFurnitureMenu() {
        furnitureMenuButton.setGraphic(IconFactory.create(IconFactory.Icon.SOFA));
        furnitureMenuButton.setText("");
        furnitureMenuButton.setTooltip(new Tooltip("Mobilya türü seç"));
        furnitureMenuButton.getStyleClass().addAll("tool-success", "furniture-picker");
        furnitureMenuButton.setMaxWidth(52);
        furnitureMenuButton.setMinWidth(50);
        furnitureMenuButton.setPrefWidth(50);

        MenuItem random = new MenuItem("Rastgele");
        random.setOnAction(event -> {
            controller.setSelectedFurnitureTemplate(null);
            controller.getGridController().setEditMode(GridController.EditMode.OBSTACLE);
            obstacleButton.setSelected(true);
        });
        furnitureMenuButton.getItems().add(random);
        for (FurnitureTemplate template : controller.getFurnitureTemplates()) {
            MenuItem item = new MenuItem(template.name());
            item.setOnAction(event -> {
                controller.setSelectedFurnitureTemplate(template);
                controller.getGridController().setEditMode(GridController.EditMode.OBSTACLE);
                obstacleButton.setSelected(true);
            });
            furnitureMenuButton.getItems().add(item);
        }
    }

    private RadioButton algorithmRadio(String text, CleaningAlgorithm algorithm, ToggleGroup group) {
        RadioButton button = new RadioButton(text);
        button.setToggleGroup(group);
        button.getStyleClass().add("algorithm-radio");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> controller.setCleaningAlgorithm(algorithm));
        return button;
    }

    private HBox statusRow(String labelText, Label value) {
        Label label = new Label(labelText);
        HBox row = new HBox(8, label, value);
        row.getStyleClass().add("status-row");
        HBox.setHgrow(value, Priority.ALWAYS);
        return row;
    }

    private Node footer() {
        Label label = new Label("Simülasyon  •  JavaFX Projesi");
        HBox footer = new HBox(7, IconFactory.create(IconFactory.Icon.INFO), label);
        footer.getStyleClass().add("footer-row");
        return footer;
    }

    private void styleGraphicButton(Button button, IconFactory.Icon icon, String styleClass) {
        button.setGraphic(IconFactory.create(icon));
        button.getStyleClass().add(styleClass);
    }

    private void styleGraphicButton(ToggleButton button, IconFactory.Icon icon, String styleClass) {
        button.setGraphic(IconFactory.create(icon));
        button.getStyleClass().add(styleClass);
    }

    private void applyBatteryValue() {
        try {
            controller.setBatteryLevel(Integer.parseInt(batteryInput.getText().trim()));
            batteryInput.clear();
        } catch (NumberFormatException exception) {
            controller.setBatteryLevel(-1);
        }
    }

    public void update() {
        Robot robot = controller.getRobot();
        double batteryLevel = robot.getBattery().getLevel();
        positionValue.setText("(" + robot.getPosition().row() + ", " + robot.getPosition().col() + ")");
        directionValue.setText(robot.getDirection().displayName());
        batteryValue.setText(String.format("%.1f%%", batteryLevel));
        updateBatteryStyle(batteryLevel);
        batteryEventValue.setText(controller.getLastBatteryEvent());
        batteryBar.setProgress(batteryLevel / 100.0);
        stateValue.setText(toDisplayState(robot.getState()));
        selectedDirtValue.setText(controller.getGridController().getSelectedDirtType().displayName());
        obstacleButton.setDisable(robot.getState() == SimulationState.RUNNING
                || robot.getState() == SimulationState.CLEANING
                || robot.getState() == SimulationState.RETURNING_TO_CHARGER
                || robot.getState() == SimulationState.RETURNING_TO_WORK
                || robot.getState() == SimulationState.CHARGING);
        furnitureMenuButton.setDisable(obstacleButton.isDisabled());
        FurnitureTemplate selectedFurniture = controller.getSelectedFurnitureTemplate();
        furnitureMenuButton.setTooltip(new Tooltip(selectedFurniture == null
                ? "Mobilya türü: Rastgele"
                : "Mobilya türü: " + selectedFurniture.name()));
        if (obstacleButton.isDisabled() && controller.getGridController().getEditMode() == GridController.EditMode.OBSTACLE) {
            controller.getGridController().setEditMode(GridController.EditMode.DIRT);
            dirtButton.setSelected(true);
        }
        startButton.setDisable(robot.getState() == SimulationState.RUNNING
                || robot.getState() == SimulationState.CLEANING
                || robot.getState() == SimulationState.RETURNING_TO_CHARGER
                || robot.getState() == SimulationState.RETURNING_TO_WORK
                || robot.getState() == SimulationState.CHARGING);
        pauseButton.setDisable(robot.getState() == SimulationState.READY || robot.getState() == SimulationState.PAUSED);
        returnHomeButton.setDisable(robot.getState() == SimulationState.RETURNING_TO_CHARGER
                || robot.getState() == SimulationState.RETURNING_TO_WORK
                || robot.getState() == SimulationState.CHARGING
                || robot.getPosition().equals(controller.getRoom().getChargingStation().getPosition()));
    }

    private void updateBatteryStyle(double batteryLevel) {
        batteryValue.getStyleClass().removeAll("battery-high", "battery-medium", "battery-low");
        batteryBar.getStyleClass().removeAll("battery-high", "battery-medium", "battery-low");
        String levelClass;
        if (batteryLevel <= 20) {
            levelClass = "battery-low";
        } else if (batteryLevel <= 50) {
            levelClass = "battery-medium";
        } else {
            levelClass = "battery-high";
        }
        batteryValue.getStyleClass().add(levelClass);
        batteryBar.getStyleClass().add(levelClass);
    }

    private String toDisplayState(SimulationState state) {
        return switch (state) {
            case READY -> "Hazır";
            case RUNNING -> "Çalışıyor";
            case PAUSED -> "Duraklatıldı";
            case CLEANING -> "Temizliyor";
            case RETURNING_TO_CHARGER -> "Şarja Dönüyor";
            case CHARGING -> "Şarj Oluyor";
            case RETURNING_TO_WORK -> "Kaldığı Yere Dönüyor";
            case ERROR -> "Hata";
        };
    }
}
