package com.robotvacuum.view;

import com.robotvacuum.controller.SimulationController;
import com.robotvacuum.model.Room;
import com.robotvacuum.service.SimulationStats;
import com.robotvacuum.service.StatisticsService;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class StatusPanelView extends HBox {
    private final SimulationController controller;
    private final Label totalArea = valueLabel();
    private final Label cleanedArea = valueLabel();
    private final Label remainingArea = valueLabel();
    private final Label elapsedTime = valueLabel();
    private final Label dirtRatio = valueLabel();

    public StatusPanelView(SimulationController controller) {
        this.controller = controller;
        getStyleClass().add("status-panel");
        setPadding(new Insets(5, 10, 5, 10));
        setSpacing(8);
        build();
    }

    private void build() {
        getChildren().addAll(
                metric("Toplam Alan", totalArea, IconFactory.Icon.AREA, "metric-area"),
                separator(),
                metric("Temizlenen Alan", cleanedArea, IconFactory.Icon.CLEANED, "metric-cleaned"),
                separator(),
                metric("Kalan Alan", remainingArea, IconFactory.Icon.DIRTY, "metric-dirty"),
                separator(),
                metric("Geçen Süre", elapsedTime, IconFactory.Icon.CLOCK, "metric-time"),
                separator(),
                metric("Toplanan Toz", dirtRatio, IconFactory.Icon.BROOM, "metric-dust")
        );
    }

    private HBox metric(String title, Label value, IconFactory.Icon icon, String styleClass) {
        Label label = new Label(title);
        label.getStyleClass().add("stat-title");
        Node iconNode = IconFactory.create(icon);
        iconNode.getStyleClass().add(styleClass);
        VBox text = new VBox(2, label, value);
        HBox item = new HBox(9, iconNode, text);
        item.getStyleClass().add("stat-item");
        HBox.setHgrow(item, Priority.ALWAYS);
        item.setMaxWidth(Double.MAX_VALUE);
        return item;
    }

    private Region separator() {
        Region separator = new Region();
        separator.getStyleClass().add("stat-separator");
        return separator;
    }

    private static Label valueLabel() {
        Label label = new Label("-");
        label.getStyleClass().add("stat-value");
        return label;
    }

    public void update() {
        Room room = controller.getRoom();
        StatisticsService statistics = controller.getStatisticsService();
        SimulationStats stats = statistics.calculate(room, controller.getRobot());
        totalArea.setText(stats.totalArea() + " hücre");
        cleanedArea.setText(String.format("%.1f%%", stats.cleanedPercentage()));
        remainingArea.setText(String.format("%d hücre (%.1f%%)",
                stats.remainingCleanableArea(),
                stats.remainingCleanablePercentage()));
        elapsedTime.setText(formatTime(controller.getElapsedSeconds()));
        dirtRatio.setText(String.format("%.1f%%", stats.collectedDirtPercentage()));
    }

    private String formatTime(int elapsedSeconds) {
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
