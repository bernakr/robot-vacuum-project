package com.robotvacuum.view;

import com.robotvacuum.controller.SimulationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {
    private final SimulationController controller;
    private final GridView gridView;
    private final ControlPanelView controlPanelView;
    private final StatusPanelView statusPanelView;
    private final Label messageBanner = new Label();

    public MainView(SimulationController controller) {
        this.controller = controller;
        this.gridView = new GridView(controller.getGridController());
        this.controlPanelView = new ControlPanelView(controller);
        this.statusPanelView = new StatusPanelView(controller);

        getStyleClass().add("app-root");
        buildLayout();
        controller.setOnChange(this::refresh);
        controller.setOnMessage(this::showWarning);
        refresh();
    }

    private void buildLayout() {
        Node robotIcon = titleRobotIcon();
        robotIcon.getStyleClass().add("title-robot-icon");
        Label title = new Label("Robot Süpürge Simülasyonu");
        title.getStyleClass().add("main-title");
        Label sparkle = new Label("✦");
        sparkle.getStyleClass().add("title-sparkle");
        HBox titleBar = new HBox(18, robotIcon, title, sparkle);
        titleBar.getStyleClass().add("title-bar");
        titleBar.setAlignment(Pos.CENTER);

        gridView.setMinHeight(0);
        gridView.setPrefHeight(610);
        gridView.setMaxHeight(Region.USE_PREF_SIZE);
        statusPanelView.setMinHeight(54);
        statusPanelView.setPrefHeight(60);
        statusPanelView.setMaxHeight(Region.USE_PREF_SIZE);

        messageBanner.getStyleClass().add("message-banner");
        messageBanner.setVisible(false);
        messageBanner.setManaged(false);
        messageBanner.setMaxWidth(Double.MAX_VALUE);

        VBox centerStack = new VBox(7, titleBar, messageBanner, gridView, statusPanelView);
        centerStack.setPadding(new Insets(8, 12, 8, 12));

        setLeft(controlPanelView);
        setCenter(centerStack);
    }

    private Node titleRobotIcon() {
        Image image = ImageAssets.load("title_robot.png");
        if (image == null) {
            Node fallback = IconFactory.create(IconFactory.Icon.ROBOT);
            fallback.setScaleX(1.8);
            fallback.setScaleY(1.8);
            return fallback;
        }

        ImageView view = new ImageView(image);
        view.setFitWidth(58);
        view.setFitHeight(58);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }

    private void refresh() {
        gridView.render(controller.getRoom(), controller.getRobot(), controller.shouldShowCompletionOverlay());
        controlPanelView.update();
        statusPanelView.update();
    }

    private void showWarning(String message) {
        if (message == null || message.isBlank()) {
            messageBanner.setText("");
            messageBanner.setVisible(false);
            messageBanner.setManaged(false);
            return;
        }
        messageBanner.setText(message);
        messageBanner.setVisible(true);
        messageBanner.setManaged(true);
    }
}
