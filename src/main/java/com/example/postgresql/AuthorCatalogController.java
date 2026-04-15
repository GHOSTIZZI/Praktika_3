package com.example.postgresql;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class AuthorCatalogController {

    @FXML
    private Label authorNameLabel;

    @FXML
    private VBox tracksPane;

    private Form1 mainController;
    private String authorName;
    private List<Track> authorTracks = Collections.emptyList();

    private final TrackDAO trackDAO = new TrackDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();

    private Image favoriteFill;
    private Image favoriteUnfill;

    //---------------------------------------------------------
    // Сеттеры и инициализация
    //---------------------------------------------------------

    public void setMainController(Form1 controller) {
        this.mainController = controller;
        favoriteFill = new Image(getClass().getResource("/images/favoritefill.png").toExternalForm());
        favoriteUnfill = new Image(getClass().getResource("/images/favoriteunfill.png").toExternalForm());
    }

    public void setAuthor(String authorName) {
        this.authorName = authorName;
        authorNameLabel.setText("Песни автора: " + authorName);
        loadTracks();
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }

    //---------------------------------------------------------
    // Загрузка треков
    //---------------------------------------------------------

    private void loadTracks() {
        tracksPane.getChildren().clear();

        Label loadingLabel = new Label("Загрузка треков автора");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        tracksPane.getChildren().add(loadingLabel);

        authorDAO.getAuthorIdByName(authorName)
                .thenCompose(authorId -> {
                    if (authorId > 0) {
                        return trackDAO.getTracksByAuthorId(authorId);
                    } else {
                        return CompletableFuture.completedFuture(Collections.<Track>emptyList());
                    }
                })
                .thenAccept(tracks -> {
                    Platform.runLater(() -> {
                        tracksPane.getChildren().clear();
                        authorTracks = tracks;

                        if (tracks.isEmpty()) {
                            tracksPane.getChildren().add(new Label("Треки не найдены."));
                        } else {
                            for (Track track : tracks) {
                                tracksPane.getChildren().add(createTrackRow(track));
                            }
                        }
                    });
                })
                .exceptionally((Throwable ex) -> {
                    Platform.runLater(() -> {
                        tracksPane.getChildren().clear();
                        String errorMsg = (ex.getCause() instanceof CancellationException) ?
                                "Операция отменена." : "Ошибка загрузки треков: " + ex.getMessage();
                        tracksPane.getChildren().add(new Label(errorMsg));
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    //---------------------------------------------------------
    // Создание строки трека с сердечком
    //---------------------------------------------------------

    private HBox createTrackRow(Track track) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-alignment: CENTER_LEFT; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        ImageView cover = new ImageView();
        cover.setFitWidth(80);
        cover.setFitHeight(80);
        cover.setPreserveRatio(true);
        try {
            if (track.getCoverUrl() != null && !track.getCoverUrl().trim().isEmpty()) {
                cover.setImage(new Image(track.getCoverUrl(), 80, 80, true, true));
            } else {
                cover.setOpacity(0.3);
            }
        } catch (Exception e) {
            cover.setOpacity(0.3);
        }

        VBox infoBox = new VBox(4);
        Text title = new Text(track.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-fill: #ffffff; -fx-font-weight: bold;");
        Text artist = new Text(track.getArtist());
        artist.setStyle("-fx-font-size: 14px; -fx-fill: #b3b3b3;");
        infoBox.getChildren().addAll(title, artist);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Button favoriteButton = new Button();
        favoriteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        ImageView heartIcon = new ImageView(favoriteUnfill);
        heartIcon.setFitWidth(25);
        heartIcon.setFitHeight(25);
        favoriteButton.setGraphic(heartIcon);
        favoriteButton.setDisable(true);

        setupFavoriteButtonAsync(favoriteButton, track);

        favoriteButton.setOnAction(e -> toggleFavoriteStatusAsync(favoriteButton, track));

        card.setOnMouseClicked(event -> {
            if (mainController != null && authorTracks != null && !authorTracks.isEmpty()) {
                mainController.openTrackDetail(track, authorTracks);
            }
        });

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color:#2a2a2a; -fx-background-radius:8; -fx-padding:10; -fx-alignment:CENTER_LEFT;")
        );

        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color:#1e1e1e; -fx-background-radius:8; -fx-padding:10; -fx-alignment:CENTER_LEFT;")
        );

        card.getChildren().addAll(cover, infoBox, favoriteButton);
        return card;
    }

    //---------------------------------------------------------
    // Логика работы сердечка
    //---------------------------------------------------------

    private void setupFavoriteButtonAsync(Button button, Track track) {
        if (mainController == null || mainController.getCurrentUserId() <= 0) {
            button.setVisible(false);
            return;
        }

        int userId = mainController.getCurrentUserId();
        button.setDisable(true);

        favoriteDAO.isFavorite(userId, track.getId())
                .thenAccept(isFav -> Platform.runLater(() -> {
                    button.setDisable(false);
                    updateFavoriteButtonUI(button, isFav);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        button.setDisable(true);
                        showError("Не удалось проверить статус избранного: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void toggleFavoriteStatusAsync(Button button, Track track) {
        int userId = mainController.getCurrentUserId();
        if (userId <= 0) return;

        button.setDisable(true);
        ImageView icon = (ImageView) button.getGraphic();
        boolean isCurrentlyFavorite = icon.getImage() == favoriteFill;

        CompletableFuture<Boolean> future = isCurrentlyFavorite
                ? favoriteDAO.removeFromFavorites(userId, track.getId())
                : favoriteDAO.addToFavorites(userId, track.getId());

        future.thenAccept(success -> Platform.runLater(() -> {
            button.setDisable(false);
            if (success) {
                updateFavoriteButtonUI(button, !isCurrentlyFavorite);
            } else {
                showError("Ошибка при обновлении избранного.");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                button.setDisable(false);
                showError("Сетевая ошибка при обновлении избранного: " + ex.getMessage());
            });
            return null;
        });
    }

    private void updateFavoriteButtonUI(Button button, boolean isFav) {
        ImageView heartIcon = new ImageView(isFav ? favoriteFill : favoriteUnfill);
        heartIcon.setFitWidth(25);
        heartIcon.setFitHeight(25);
        button.setGraphic(heartIcon);
    }

    //---------------------------------------------------------
    // Утилиты
    //---------------------------------------------------------

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}