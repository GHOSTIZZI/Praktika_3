package com.example.postgresql;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.Comparator; // 🛑 ДОБАВЛЕНО
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

public class IzbranController {

    @FXML
    private VBox favoriteTracksVBox;
    @FXML
    private ChoiceBox<String> sortChoiceBox;

    private Form1 mainController;
    private List<Track> favoriteTracks = Collections.emptyList();

    private final TrackDAO trackDAO = new TrackDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();

    private final Map<String, String> sortOptions = new LinkedHashMap<>();

    private ImageView favoriteFillIcon;
    private ImageView favoriteUnfillIcon;

    //---------------------------------------------------------
    // Инициализация и Сеттеры
    //---------------------------------------------------------

    @FXML
    public void initialize() {
        sortOptions.put("По названию песни (А-Я)", "title.asc");
        sortOptions.put("По исполнителю (А-Я)", "artist.asc");

        sortChoiceBox.setItems(FXCollections.observableArrayList(sortOptions.keySet()));
        sortChoiceBox.getSelectionModel().selectFirst();

        sortChoiceBox.setOnAction(event -> handleSortChange());

        favoriteFillIcon = new ImageView(new Image(getClass().getResource("/images/favoritefill.png").toExternalForm()));
        favoriteUnfillIcon = new ImageView(new Image(getClass().getResource("/images/favoriteunfill.png").toExternalForm()));

        favoriteFillIcon.setFitWidth(25);
        favoriteFillIcon.setFitHeight(25);

        favoriteUnfillIcon.setFitWidth(25);
        favoriteUnfillIcon.setFitHeight(25);
    }

    public void setMainController(Form1 controller) {
        this.mainController = controller;
        loadFavoriteTracks();
    }

    //---------------------------------------------------------
    // Основная логика загрузки и сортировки
    //---------------------------------------------------------

    private void loadFavoriteTracks() {
        if (mainController == null) return;

        int userId = mainController.getCurrentUserId();
        if (userId <= 0) {
            showError("Пользователь не авторизован.");
            return;
        }

        String selectedOption = sortChoiceBox.getSelectionModel().getSelectedItem();


        favoriteTracksVBox.getChildren().clear();
        Label loadingLabel = new Label("Загрузка избранного");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        favoriteTracksVBox.getChildren().add(loadingLabel);


        trackDAO.getFavoriteTracksByUser(userId, selectedOption)
                .thenAccept(tracks -> {


                    List<Track> sortedTracks = sortTracksInJava(tracks, selectedOption);

                    Platform.runLater(() -> {
                        favoriteTracksVBox.getChildren().clear();
                        favoriteTracks = sortedTracks;

                        if (sortedTracks.isEmpty()) {
                            Text emptyMessage = new Text("Список избранного пуст.");
                            emptyMessage.setStyle("-fx-fill: white; -fx-font-size: 16px;");
                            favoriteTracksVBox.getChildren().add(emptyMessage);
                        } else {
                            for (Track track : sortedTracks) {
                                HBox card = createFavoriteCard(track, userId);
                                favoriteTracksVBox.getChildren().add(card);
                            }
                        }
                    });
                })
                .exceptionally((Throwable ex) -> {
                    Platform.runLater(() -> {
                        favoriteTracksVBox.getChildren().clear();
                        if (!(ex.getCause() instanceof CancellationException)) {

                            System.err.println("Ошибка при загрузке избранного:");
                            ex.printStackTrace();
                            showError("Ошибка загрузки избранного: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                        }
                    });
                    return null;
                });
    }


    @FXML
    private void handleSortChange() {
        loadFavoriteTracks();
    }


    private List<Track> sortTracksInJava(List<Track> tracks, String sortKey) {
        if (tracks == null || sortKey == null) {
            return tracks;
        }


        Comparator<Track> comparator;

        if (sortKey.contains("исполнителю")) {
            comparator = Comparator.comparing(Track::getArtist,
                    Comparator.nullsLast(String::compareTo));
        } else {
            comparator = Comparator.comparing(Track::getTitle,
                    Comparator.nullsLast(String::compareTo));
        }

        tracks.sort(comparator);
        return tracks;
    }


    //---------------------------------------------------------
    // Создание карточки и Удаление
    //---------------------------------------------------------

    private HBox createFavoriteCard(Track track, int userId) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #1e1e1e;; -fx-background-radius: 8; -fx-padding: 10; "
                + "-fx-alignment: CENTER_LEFT; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
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


        Button removeButton = new Button();
        ImageView heartIcon = new ImageView(
                new Image(getClass().getResource("/images/favoritefill.png").toExternalForm())
        );

        heartIcon.setFitWidth(25);
        heartIcon.setFitHeight(25);

        removeButton.setGraphic(heartIcon);

        removeButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;"
        );
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color:#2a2a2a; -fx-background-radius:8; -fx-padding:10; -fx-alignment:CENTER_LEFT;")
        );

        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color:#1e1e1e; -fx-background-radius:8; -fx-padding:10; -fx-alignment:CENTER_LEFT;")
        );

        removeButton.setOnAction(e -> {
            removeButton.setDisable(true);

            favoriteDAO.removeFromFavorites(userId, track.getId())
                    .thenAccept((Boolean success) -> {
                        Platform.runLater(() -> {
                            removeButton.setDisable(false);
                            if (success) {
                                loadFavoriteTracks();
                            } else {
                                showError("Не удалось удалить из избранного.");
                            }
                        });
                    })
                    .exceptionally((Throwable ex) -> {
                        Platform.runLater(() -> {
                            removeButton.setDisable(false);
                            showError("Ошибка удаления из избранного: " + ex.getMessage());
                        });
                        return null;
                    });
        });


        card.setOnMouseClicked(event -> {
            if (mainController != null && favoriteTracks != null && !favoriteTracks.isEmpty()) {
                mainController.openTrackDetail(track, favoriteTracks);
            }
        });

        card.getChildren().addAll(cover, infoBox, removeButton);
        return card;
    }

    //---------------------------------------------------------
    // Утилиты и Навигация
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

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}