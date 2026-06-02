package com.robotvacuum.view;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

public final class IconFactory {
    public enum Icon {
        TOOLS,
        DUST,
        LIQUID,
        STAIN,
        SOFA,
        SPEED,
        GEAR,
        ROBOT,
        GAMEPAD,
        PLAY,
        PAUSE,
        STOP,
        HOME,
        INFO,
        ARROW_RIGHT,
        CLOCK,
        BROOM,
        AREA,
        CLEANED,
        DIRTY
    }

    private IconFactory() {
    }

    public static Node create(Icon icon) {
        if (icon == Icon.BROOM) {
            return createBroom();
        }

        SVGPath path = new SVGPath();
        path.setContent(path(icon));
        path.getStyleClass().add("icon-shape");
        path.setScaleX(0.72);
        path.setScaleY(0.72);

        StackPane box = new StackPane(path);
        box.getStyleClass().add("icon-box");
        box.setMinSize(22, 22);
        box.setPrefSize(22, 22);
        box.setMaxSize(22, 22);
        return box;
    }

    private static Node createBroom() {
        Rectangle handle = new Rectangle(9.4, 1.8, 3.1, 14.2);
        handle.setFill(Color.web("#ff9b29"));
        handle.setRotate(18);
        handle.setArcWidth(2);
        handle.setArcHeight(2);

        Polygon head = new Polygon(
                6.2, 13.2,
                15.9, 15.1,
                18.7, 21.2,
                3.7, 21.2
        );
        head.setFill(Color.web("#ffc53d"));
        head.setStroke(Color.web("#ff8a18"));
        head.setStrokeWidth(1.0);

        Line band = new Line(6.9, 14.0, 15.4, 15.8);
        band.setStroke(Color.web("#ff8a18"));
        band.setStrokeWidth(2.0);

        Line bristleOne = new Line(7.3, 16.3, 5.1, 21.0);
        Line bristleTwo = new Line(10.3, 16.7, 9.2, 21.1);
        Line bristleThree = new Line(13.4, 17.1, 14.9, 21.1);
        for (Line line : new Line[]{bristleOne, bristleTwo, bristleThree}) {
            line.setStroke(Color.web("#e17314"));
            line.setStrokeWidth(1.2);
        }

        StackPane box = new StackPane(handle, head, band, bristleOne, bristleTwo, bristleThree);
        box.getStyleClass().addAll("icon-box", "broom-icon");
        box.setMinSize(22, 22);
        box.setPrefSize(22, 22);
        box.setMaxSize(22, 22);
        return box;
    }

    private static String path(Icon icon) {
        return switch (icon) {
            case TOOLS -> "M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9C10.1 1 7.4.4 5.1 1.3l4.1 4.1-3.7 3.7-4-4C.4 7.4 1 10.1 3 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.4-.4.4-1 0-1.4z";
            case DUST -> "M5 4a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm9 1.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM7 13a3 3 0 1 0 0 6 3 3 0 0 0 0-6zm11 2a2 2 0 1 0 0 4 2 2 0 0 0 0-4z";
            case LIQUID -> "M12 2C8 7.2 6 10.2 6 14a6 6 0 0 0 12 0c0-3.8-2-6.8-6-12zm0 18a4 4 0 0 1-4-4c0-2.4 1.2-4.6 4-8.5 2.8 3.9 4 6.1 4 8.5a4 4 0 0 1-4 4z";
            case STAIN -> "M12 2l1.8 4.2L18 4.4l-1.8 4.2L20 11l-4.6.4L17 16l-4.1-2.4L10 20l-1-6.4L4 16l2.4-4.6L2 11l3.8-2.4L4 4.4l4.2 1.8L12 2z";
            case SOFA -> "M4 10V8a3 3 0 0 1 3-3h10a3 3 0 0 1 3 3v2a3 3 0 0 1 2 2.8V19h-3v-2H5v2H2v-6.2A3 3 0 0 1 4 10zm3-3a1 1 0 0 0-1 1v4h12V8a1 1 0 0 0-1-1H7zm-2 7v3h14v-3H5z";
            case SPEED -> "M12 4a10 10 0 0 0-9.8 12h3.1A7 7 0 1 1 19 16h3.1A10 10 0 0 0 12 4zm0 4l-4 7h5l3-5-4-2z";
            case GEAR -> "M19.4 13.5c.1-.5.1-1 .1-1.5s0-1-.1-1.5l2.1-1.6-2-3.5-2.5 1a8 8 0 0 0-2.6-1.5L14 2h-4l-.4 2.9A8 8 0 0 0 7 6.4l-2.5-1-2 3.5 2.1 1.6A9 9 0 0 0 4.5 12c0 .5 0 1 .1 1.5L2.5 15.1l2 3.5 2.5-1a8 8 0 0 0 2.6 1.5L10 22h4l.4-2.9a8 8 0 0 0 2.6-1.5l2.5 1 2-3.5-2.1-1.6zM12 15.5A3.5 3.5 0 1 1 12 8a3.5 3.5 0 0 1 0 7.5z";
            case ROBOT -> "M8 4h8v3h2a3 3 0 0 1 3 3v7a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3v-7a3 3 0 0 1 3-3h2V4zm2 0h4v3h-4V4zM6 9a1 1 0 0 0-1 1v7a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-7a1 1 0 0 0-1-1H6zm2.5 4a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zm7 0a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zM9 16h6v2H9v-2z";
            case GAMEPAD -> "M7 8h10a5 5 0 0 1 4.7 3.3l1.1 3.1a3 3 0 0 1-5.2 2.8L15.8 15H8.2l-1.8 2.2a3 3 0 0 1-5.2-2.8l1.1-3.1A5 5 0 0 1 7 8zm-1 4v2h2v2h2v-2h2v-2h-2v-2H8v2H6zm11.5 1a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z";
            case PLAY -> "M6 4l14 8-14 8V4z";
            case PAUSE -> "M6 5h5v14H6V5zm8 0h5v14h-5V5z";
            case STOP -> "M6 6h12v12H6V6z";
            case HOME -> "M3 11l9-8 9 8h-3v9h-5v-6h-2v6H6v-9H3z";
            case INFO -> "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";
            case ARROW_RIGHT -> "M4 11h11.2L10 5.8 11.8 4 20 12l-8.2 8-1.8-1.8 5.2-5.2H4v-2z";
            case CLOCK -> "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm0 2.4a7.6 7.6 0 1 1 0 15.2 7.6 7.6 0 0 1 0-15.2zM13 7h-2v6.3l4.7 2.8 1-1.7-3.7-2.2V7z";
            case BROOM -> "M15.7 2l2.5 1.1-4.6 10.2-2.5-1.1L15.7 2zM10.2 12.4l5.4 2.4-.9 2-5.4-2.4.9-2zM8.7 14.9l5.7 2.5-1.9 4.3H3.8l1.4-3.1 1.6.7 1.3-3-1.7-.8.6-1.3 1.7.7zm1.2 2.3l-1.4 3.2h1.4l1.1-2.6-1.1-.6zm-3.2 3.2l1.2-2.7-1-.4-1.4 3.1h1.2z";
            case AREA -> "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z";
            case CLEANED -> "M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z";
            case DIRTY -> "M6 7a3 3 0 1 0 0 6 3 3 0 0 0 0-6zm8-3a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm3 10a3 3 0 1 0 0 6 3 3 0 0 0 0-6zM9 16a2 2 0 1 0 0 4 2 2 0 0 0 0-4z";
        };
    }
}
