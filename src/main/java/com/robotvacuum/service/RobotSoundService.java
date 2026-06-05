package com.robotvacuum.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class RobotSoundService {

    private MediaPlayer movementPlayer;

    public RobotSoundService() {
        try {

            Media media = new Media(
                    getClass()
                            .getResource("/assets/sounds/robot_sounds.mp3")
                            .toExternalForm());

            movementPlayer = new MediaPlayer(media);

            // sürekli tekrar etsin
            movementPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            // ses seviyesi
            movementPlayer.setVolume(0.12);

        } catch (Exception e) {
            System.out.println("Ses yüklenemedi: " + e.getMessage());
        }
    }

    public void setPlaybackRate(double speed) {
        if (movementPlayer != null) {
            movementPlayer.setRate(speed);
        }
    }

    // robot hareket etmeye başlayınca
    public void startMovementSound() {
        if (movementPlayer != null) {
            movementPlayer.play();
        }
    }

    // durdurunca
    public void pauseMovementSound() {
        if (movementPlayer != null) {
            movementPlayer.pause();
        }
    }

    // tamamen sıfırlayınca
    public void stopMovementSound() {
        if (movementPlayer != null) {
            movementPlayer.stop();
        }
    }
}