package com.focusnode.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class AudioService {

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    public void playBinauralBeats() {
        if (isPlaying) return;

        try {
            URL resource = getClass().getResource("/audio/binaural.mp3");
            if (resource == null) {
                System.out.println("[AudioService] binaural.mp3 not found in resources/audio/. Binaural Beats will not play.");
                return;
            }
            
            Media media = new Media(resource.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.5); // 50% volume for background
            mediaPlayer.play();
            isPlaying = true;
            System.out.println("[AudioService] Playing Binaural Beats.");
        } catch (Exception e) {
            System.err.println("[AudioService] Failed to play audio: " + e.getMessage());
        }
    }

    public void stopBinauralBeats() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            isPlaying = false;
            System.out.println("[AudioService] Stopped Binaural Beats.");
        }
    }
}
