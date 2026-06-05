package com.robotvacuum;

import com.robotvacuum.controller.SimulationController;
import com.robotvacuum.view.MainView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        SimulationController controller = new SimulationController();
        MainView mainView = new MainView(controller);
        Scene scene = new Scene(mainView, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        stage.setTitle("Robot Süpürge Simülasyonu");
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
