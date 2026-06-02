package com.robotvacuum.view;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class ImageAssets {
    private static final Map<String, Image> CACHE = new HashMap<>();

    private ImageAssets() {
    }

    public static Image load(String fileName) {
        return CACHE.computeIfAbsent(fileName, ImageAssets::loadImage);
    }

    public static Image furniture(String name) {
        if (name.contains("Koltuk") && !name.contains("Tekli")) {
            return load("sofa_double.png");
        }
        if (name.contains("Tekli")) {
            return load("armchair.png");
        }
        if (name.contains("Orta Masa")) {
            return load("coffee_table.png");
        }
        if (name.contains("Yemek") || name.contains("Çalışma")) {
            return load("dining_table.png");
        }
        if (name.contains("Konsol")) {
            return load("console.png");
        }
        if (name.contains("Dolap") || name.contains("Kitaplık")) {
            return load("bookcase.png");
        }
        if (name.contains("Bitki")) {
            return load("plant.png");
        }
        return null;
    }

    private static Image loadImage(String fileName) {
        String path = "/assets/" + fileName;
        try (InputStream stream = ImageAssets.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            Image image = new Image(stream);
            return removeMagentaKey(image);
        } catch (Exception exception) {
            return null;
        }
    }

    private static Image removeMagentaKey(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        if (width <= 0 || height <= 0) {
            return image;
        }

        PixelReader reader = image.getPixelReader();
        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                writer.setColor(x, y, isMagentaKey(color) ? Color.TRANSPARENT : color);
            }
        }
        return output;
    }

    private static boolean isMagentaKey(Color color) {
        return color.getRed() > 0.72
                && color.getBlue() > 0.72
                && color.getGreen() < 0.42
                && Math.abs(color.getRed() - color.getBlue()) < 0.32;
    }
}
