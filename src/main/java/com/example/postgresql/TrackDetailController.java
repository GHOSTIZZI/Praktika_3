package com.example.postgresql;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;

public class TrackDetailController {


    private final PlaylistService playlistService = new PlaylistService();




    @FXML private ImageView coverImage;
    @FXML private Label titleLabel;
    @FXML private Label artistLabel;

    @FXML private Button favoriteButton;
    @FXML private Slider progressSlider;
    @FXML private Label currentTimeLabel;
    @FXML private Label durationLabel;

    @FXML private Button loopButton; // кнопка в FXML для переключения режима
    private boolean isLooping = false; // состояние повторения
    private ImageView loopIcon;        // иконка для кнопки
    private ImageView loopActiveIcon;  // иконка активного состояния


    @FXML private Slider volumeSlider;

    private Track track;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isSliderBeingDragged = false;
    private Form1 mainController;

    private int currentTrackIndex = -1;
    private List<Track> allTracks;

    private long lastPrevClickTime = 0;
    private static final long DOUBLE_CLICK_THRESHOLD = 300;

    private final FavoriteDAO favoriteDAO = new FavoriteDAO();

    @FXML private Button prevButton;
    @FXML private Button playPauseButton;
    @FXML private Button nextButton;
    @FXML private Button libraryadd;


    private ImageView favoriteFillIcon;
    private ImageView favoriteUnfillIcon;
    private ImageView playIcon;
    private ImageView pauseIcon;
    private ImageView prevIcon;
    private ImageView nextIcon;
    private ImageView libraryaddicon;

    @FXML
    private void initialize() {
        playIcon = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
        pauseIcon = new ImageView(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
        prevIcon = new ImageView(new Image(getClass().getResource("/images/rewind.png").toExternalForm()));
        nextIcon = new ImageView(new Image(getClass().getResource("/images/forward.png").toExternalForm()));
        libraryaddicon = new ImageView(new Image(getClass().getResource("/images/library_add.png").toExternalForm()));

        playIcon.setFitWidth(80);  playIcon.setFitHeight(80);
        pauseIcon.setFitWidth(80); pauseIcon.setFitHeight(80);
        prevIcon.setFitWidth(40);  prevIcon.setFitHeight(50);
        nextIcon.setFitWidth(40);  nextIcon.setFitHeight(50);
        libraryaddicon.setFitWidth(27);  libraryaddicon.setFitHeight(27);

        playPauseButton.setGraphic(playIcon);
        prevButton.setGraphic(prevIcon);
        nextButton.setGraphic(nextIcon);
        libraryadd.setGraphic(libraryaddicon);

        favoriteFillIcon = new ImageView(new Image(getClass().getResource("/images/favoritefill.png").toExternalForm()));
        favoriteUnfillIcon = new ImageView(new Image(getClass().getResource("/images/favoriteunfill.png").toExternalForm()));
        favoriteFillIcon.setFitWidth(30); favoriteFillIcon.setFitHeight(30);
        favoriteUnfillIcon.setFitWidth(30); favoriteUnfillIcon.setFitHeight(30);

        loopIcon = new ImageView(new Image(getClass().getResource("/images/loop.png").toExternalForm()));
        loopActiveIcon = new ImageView(new Image(getClass().getResource("/images/loopactive.png").toExternalForm()));

        loopIcon.setFitWidth(25); loopIcon.setFitHeight(20);
        loopActiveIcon.setFitWidth(25); loopActiveIcon.setFitHeight(25);

        loopButton.setGraphic(loopIcon);


        artistLabel.setOnMouseClicked(event -> {
            if (track != null && mainController != null) {
                mainController.openAuthorCatalog(track.getArtist());
            }
        });


    }

    //---------------------------------------------------------
    // Сеттеры и Геттеры
    //---------------------------------------------------------

    public void setTrackList(List<Track> tracks) {
        this.allTracks = tracks;
    }

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        disposeMediaPlayer();
        this.track = track;

        if (allTracks != null) {
            currentTrackIndex = -1;
            for (int i = 0; i < allTracks.size(); i++) {
                if (allTracks.get(i).getId() == track.getId()) {
                    currentTrackIndex = i;
                    break;
                }
            }
        }

        loadTrackVisuals();
        setupFavoriteButton();
        setupMediaPlayer();

        updatePlayPauseUI(false);
    }

    //---------------------------------------------------------
    // UI и MediaPlayer
    //---------------------------------------------------------

    private void loadTrackVisuals() {
        titleLabel.setText(track.getTitle());
        artistLabel.setText(track.getArtist());

        try {
            if (track.getCoverUrl() != null && !track.getCoverUrl().trim().isEmpty()) {
                coverImage.setImage(new Image(track.getCoverUrl(), true));
            } else {
                coverImage.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки обложки: " + track.getCoverUrl());
        }
    }

    private void setupFavoriteButton() {
        if (mainController == null || mainController.getCurrentUserId() <= 0 || track == null) {
            favoriteButton.setVisible(false);
            return;
        }
        favoriteButton.setVisible(true);

        favoriteButton.setText("");
        favoriteButton.setDisable(true);

        int userId = mainController.getCurrentUserId();

        favoriteDAO.isFavorite(userId, track.getId())
                .thenAccept(isFav -> Platform.runLater(() -> {
                    favoriteButton.setDisable(false);
                    updateFavoriteButtonUI(isFav);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        favoriteButton.setText("Ошибка");
                        favoriteButton.setDisable(false);
                        if (!(ex.getCause() instanceof CancellationException) && mainController != null) {
                            mainController.showError("Не удалось проверить статус избранного: " + ex.getMessage());
                        }
                    });
                    return null;
                });
    }

    private void updateFavoriteButtonUI(boolean isFav) {

        favoriteButton.getStyleClass().removeAll("favorite-added", "favorite-not-added");

        if (isFav) {
            favoriteButton.setGraphic(favoriteFillIcon);
            favoriteButton.setText("");
            favoriteButton.getStyleClass().add("favorite-added");
        } else {
            favoriteButton.setGraphic(favoriteUnfillIcon);
            favoriteButton.setText("");
            favoriteButton.getStyleClass().add("favorite-not-added");
        }
    }

    private void setupMediaPlayer() {
        if (track == null || track.getAudioUrl() == null || track.getAudioUrl().isBlank()) {
            playPauseButton.setText("Ошибка");
            if (mainController != null) {
                mainController.showError("URL аудиофайла пуст или отсутствует.");
            }
            return;
        }

        try {
            disposeMediaPlayer();
            if (mainController != null) {
                mainController.setCurrentMediaPlayer(null);
            }

            Media media = new Media(track.getAudioUrl());
            mediaPlayer = new MediaPlayer(media);

            media.setOnError(() -> Platform.runLater(() -> {
                playPauseButton.setText("Ошибка Media");
                if (mainController != null) {
                    mainController.showError("Ошибка загрузки медиа: " + media.getError().getMessage());
                }
            }));

            mediaPlayer.setOnError(() -> Platform.runLater(() -> {
                playPauseButton.setText("Ошибка Плеера");
                if (mainController != null) {
                    mainController.showError("Ошибка воспроизведения: " + mediaPlayer.getError().getMessage());
                }
            }));

            mediaPlayer.setVolume(volumeSlider.getValue());
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                    mediaPlayer.setVolume(newVal.doubleValue())
            );

            if (mainController != null) {
                mainController.setCurrentMediaPlayer(mediaPlayer);
            }

            mediaPlayer.setOnReady(() -> {
                Duration total = mediaPlayer.getMedia().getDuration();
                progressSlider.setMax(total.toSeconds());
                durationLabel.setText(formatTime(total));
                currentTimeLabel.setText("0:00");

                mediaPlayer.play();
                updatePlayPauseUI(true);
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!isSliderBeingDragged) {
                    progressSlider.setValue(newTime.toSeconds());
                    currentTimeLabel.setText(formatTime(newTime));
                }
            });

            progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                isSliderBeingDragged = isChanging;
                if (!isChanging && mediaPlayer != null) {
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                }
            });

            progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (isSliderBeingDragged && mediaPlayer != null) {
                    currentTimeLabel.setText(formatTime(Duration.seconds(newVal.doubleValue())));
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                if (isLooping) {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                } else {
                    updatePlayPauseUI(false);
                    progressSlider.setValue(0);
                    playNextTrack();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            playPauseButton.setText("Ошибка");
            if (mainController != null) {
                mainController.showError("Не удалось инициализировать плеер.");
            }
        }
    }

    //---------------------------------------------------------
    // Управление воспроизведением
    //---------------------------------------------------------

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
        if (mediaPlayer == null)
            return;

        MediaPlayer.Status status = mediaPlayer.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setGraphic(playIcon);
        } else {
            mediaPlayer.play();
            playPauseButton.setGraphic(pauseIcon);
        }
    }

    @FXML
    private void handlePrevButton() {
        long now = System.currentTimeMillis();

        if (now - lastPrevClickTime < DOUBLE_CLICK_THRESHOLD) {
            playPreviousTrack();
        } else if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.ZERO);
        }
        lastPrevClickTime = now;
    }

    @FXML
    private void handleNextButton() {
        playNextTrack();
    }

    private void playNextTrack() {
        if (allTracks == null || allTracks.isEmpty()) return;

        currentTrackIndex++;
        if (currentTrackIndex >= allTracks.size()) {
            currentTrackIndex = 0;
        }
        changeTrack(allTracks.get(currentTrackIndex), true);
    }

    private void playPreviousTrack() {
        if (allTracks == null || allTracks.isEmpty()) return;

        currentTrackIndex--;
        if (currentTrackIndex < 0) {
            currentTrackIndex = allTracks.size() - 1;
        }
        changeTrack(allTracks.get(currentTrackIndex), true);
    }

    private void changeTrack(Track newTrack, boolean shouldStartPlaying) {
        setTrack(newTrack);

        if (mediaPlayer != null) {
            if (shouldStartPlaying) {
                mediaPlayer.play();
                updatePlayPauseUI(true);
            } else {
                mediaPlayer.pause();
                updatePlayPauseUI(false);
            }
        } else {
            updatePlayPauseUI(false);
        }
    }

    @FXML
    private void addToFavorites() {
        if (mainController == null || track == null) return;

        int userId = mainController.getCurrentUserId();
        if (userId <= 0) {
            mainController.showError("Ошибка: пользователь не авторизован");
            return;
        }
        favoriteButton.setDisable(true);
        boolean isCurrentlyFavorite = favoriteButton.getStyleClass().contains("favorite-added");

        CompletableFuture<Boolean> future = isCurrentlyFavorite
                ? favoriteDAO.removeFromFavorites(userId, track.getId())
                : favoriteDAO.addToFavorites(userId, track.getId());

        future.thenAccept(success -> Platform.runLater(() -> {
                    favoriteButton.setDisable(false);
                    if (success) {
                        updateFavoriteButtonUI(!isCurrentlyFavorite);
                    } else {
                        mainController.showError("Ошибка при обновлении избранного.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        favoriteButton.setDisable(false);
                        mainController.showError("Сетевая ошибка: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void goBack() {
        disposeMediaPlayer();
        if (mainController != null) {
            mainController.setCurrentMediaPlayer(null);
            mainController.showMainView();
        }
    }

    private String formatTime(Duration duration) {
        int total = (int) duration.toSeconds();
        int min = total / 60;
        int sec = total % 60;
        return String.format("%d:%02d", min, sec);
    }

    @FXML
    private void toggleLoop() {
        isLooping = !isLooping;
        loopButton.setGraphic(isLooping ? loopActiveIcon : loopIcon);
    }








    @FXML
    private void addToPlaylist() {
        if (track == null || mainController == null) return;

        int userId = mainController.getCurrentUserId();

        playlistService.loadUserPlaylists(userId)
                .thenAccept(playlists -> Platform.runLater(() -> {
                    showPlaylistSelector(playlists);
                }));
    }

    private void showPlaylistSelector(List<Playlist> playlists) {

        ChoiceDialog<Playlist> dialog =
                new ChoiceDialog<>(playlists.isEmpty() ? null : playlists.get(0), playlists);

        dialog.setTitle(" ");

        // убираем белый header
        dialog.setHeaderText(null);

        dialog.setContentText("Выберите плейлист:");

        // перевод кнопок
        dialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);

        ButtonType addButton = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(addButton, cancelButton);

        // подключаем css
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("dialog.css").toExternalForm()
        );

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        dialog.showAndWait().ifPresent(selected -> {
            playlistService.addTrack(selected.getId(), track.getId())
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (!success) {
                            mainController.showError("Ошибка добавления");
                        }
                    }));
        });
    }

}