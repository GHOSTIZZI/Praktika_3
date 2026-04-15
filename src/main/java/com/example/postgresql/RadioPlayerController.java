package com.example.postgresql;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class RadioPlayerController {

    @FXML private ImageView coverImage;
    @FXML private Label titleLabel;
    @FXML private Button playPauseButton;
    @FXML private Slider volumeSlider;

    private RadioStation station;
    private Form1 mainController;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    @FXML
    private void initialize() {
        playIcon = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
        pauseIcon = new ImageView(new Image(getClass().getResource("/images/pause.png").toExternalForm()));

        playIcon.setFitWidth(80);
        playIcon.setFitHeight(80);

        pauseIcon.setFitWidth(80);
        pauseIcon.setFitHeight(80);

        playPauseButton.setGraphic(playIcon);
    }

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public void setStation(RadioStation station) {
        this.station = station;
        loadStationVisuals();
        setupMediaPlayer();
        updatePlayPauseUI(false);
    }

    private void loadStationVisuals() {
        titleLabel.setText(station.getName());
        try {
            if (station.getCoverUrl() != null && !station.getCoverUrl().trim().isEmpty()) {
                coverImage.setImage(new Image(station.getCoverUrl(), 300, 300, true, true));
            } else {
                coverImage.setImage(new Image("https://via.placeholder.com/300x300/7733ff/ffffff?text=Radio"));
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки обложки радио: " + station.getName());
        }
    }

    private void setupMediaPlayer() {
        if (station == null || station.getStreamUrl() == null || station.getStreamUrl().isBlank()) {
            playPauseButton.setText("Ошибка");
            return;
        }

        try {
            disposeMediaPlayer();

            if (mainController != null) {
                mainController.stopCurrentTrack();
            }

            Media media = new Media(station.getStreamUrl());
            mediaPlayer = new MediaPlayer(media);

            if (mainController != null) {
                mainController.setCurrentMediaPlayer(mediaPlayer);
            }

            mediaPlayer.setVolume(volumeSlider.getValue());
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(newVal.doubleValue());
                }
            });

            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                updatePlayPauseUI(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            playPauseButton.setText("Ошибка");
        }
    }
    private ImageView playIcon;
    private ImageView pauseIcon;
    private void updatePlayPauseUI(boolean playing) {
        playPauseButton.setGraphic(playing ? pauseIcon : playIcon);
    }

    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    @FXML
    private void togglePlayPause() {
        if (mediaPlayer == null) return;

        MediaPlayer.Status status = mediaPlayer.getStatus();

        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            updatePlayPauseUI(false);
        } else {
            mediaPlayer.play();
            updatePlayPauseUI(true);
        }
    }

    @FXML
    private void closePlayer() {
        disposeMediaPlayer();

        if (mainController != null) {
            mainController.setCurrentMediaPlayer(null);
            mainController.showMainView();
        }
    }
}