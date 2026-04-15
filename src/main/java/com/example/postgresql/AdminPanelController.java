package com.example.postgresql;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class AdminPanelController {

    @FXML private TextField titleField;
    @FXML private TextField artistField;

    @FXML private TextField albumField;
    @FXML private TextField genreField;
    @FXML private TextField coverUrlField;
    @FXML private TextField audioUrlField;
    @FXML private TableColumn<Track, String> colTrackAlbum;
    @FXML private TableColumn<Track, String> colTrackGenre;
    @FXML private TextField authorIdField;


    @FXML private Label statusLabel;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colLogin;
    @FXML private TableColumn<User, String> colRole;

    @FXML private TableView<Track> tracksTable;
    @FXML private TableColumn<Track, Integer> colTrackId;
    @FXML private TableColumn<Track, String> colTrackTitle;
    @FXML private TableColumn<Track, String> colTrackArtist;
    @FXML private TableColumn<Track, String> colTrackCover;
    @FXML private TableColumn<Track, String> colTrackAudio;
    @FXML private TextField authorNameField;
    @FXML private TextField authorPhotoUrlField;
    @FXML private Label authorStatusLabel;
    @FXML private TableView<Author> authorsTable;


    @FXML private TableView<RadioStation> radioTable;
    @FXML private TableColumn<RadioStation, Integer> colRadioId;
    @FXML private TableColumn<RadioStation, String> colRadioName;
    @FXML private TableColumn<RadioStation, String> colRadioStream;
    @FXML private TableColumn<RadioStation, String> colRadioCover;

    @FXML private TextField radioNameField;
    @FXML private TextField radioStreamField;
    @FXML private TextField radioCoverField;
    @FXML private Label radioStatusLabel;

    @FXML private TextField albumTitleField;
    @FXML private TextField albumCoverField;
    @FXML private ListView<Track> albumTracksList;
    @FXML private Label albumStatusLabel;

    private final AdminPlaylistDAO adminPlaylistDAO =
            new AdminPlaylistDAO();

    private Form1 mainController;


    private final TrackDAO trackDAO = new TrackDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();

    private List<User> allUsers;
    private List<Track> allTracks;
    private final RadioDAO radioDAO = new RadioDAO();

    public void setMainController(Form1 controller) {
        this.mainController = controller;
        loadData();
    }

    @FXML
    public void initialize() {

        colTrackId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTrackTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTrackArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));

        if (colTrackAlbum != null) {
            colTrackAlbum.setCellValueFactory(new PropertyValueFactory<>("album"));
        }
        if (colTrackGenre != null) {
            colTrackGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        }
        if (colTrackCover != null) {
            colTrackCover.setCellValueFactory(new PropertyValueFactory<>("coverUrl"));
        }
        albumTracksList.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);


        if (colTrackAudio != null) {
            colTrackAudio.setCellValueFactory(new PropertyValueFactory<>("trackUrl"));
        }
        colRadioId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRadioName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRadioStream.setCellValueFactory(new PropertyValueFactory<>("streamUrl"));
        colRadioCover.setCellValueFactory(new PropertyValueFactory<>("coverUrl"));

    }

    private void loadData() {
        loadTracksTable();
        loadUsersTable();
        loadAuthorsTable();
        loadRadioTable();
        loadTracksForAlbumCreation();
        loadAlbumsBox();
    }

    // =================================================================
    // ЛОГИКА ТРЕКОВ
    // =================================================================

    private void loadTracksTable() {
        tracksTable.getItems().clear();
        tracksTable.setPlaceholder(new ProgressIndicator());
        trackDAO.getAllTracks()
                .thenAccept(tracks -> {
                    allTracks = tracks;
                    Platform.runLater(() -> {
                        tracksTable.setPlaceholder(new Label("Нет данных о треках."));
                        tracksTable.getItems().addAll(tracks);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        tracksTable.setPlaceholder(new Label("Ошибка загрузки треков: " + ex.getMessage()));
                        mainController.showError("Ошибка загрузки треков: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void addTrack() {

        if (titleField.getText().isEmpty() || audioUrlField.getText().isEmpty() || authorIdField.getText().isEmpty()) {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Необходимо заполнить Название, ID Автора и Ссылку на аудио.");
            return;
        }

        try {

            int authorId = Integer.parseInt(authorIdField.getText().trim());


            Track newTrack = new Track();
            newTrack.setTitle(titleField.getText());
            newTrack.setCoverUrl(coverUrlField.getText());
            newTrack.setTrackUrl(audioUrlField.getText());

            newTrack.setAuthorId(authorId);


            if (albumField != null) newTrack.setAlbum(albumField.getText());
            if (genreField != null) newTrack.setGenre(genreField.getText());


            newTrack.setArtist("N/A");

            trackDAO.addTrack(newTrack)
                    .thenAccept(success -> {
                        Platform.runLater(() -> {
                            if (success) {
                                statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                                statusLabel.setText("Трек успешно добавлен.");
                                clearTrackFields();
                                loadTracksTable();
                                if (mainController != null) {
                                    mainController.refreshTracks();
                                }
                            } else {
                                statusLabel.setTextFill(javafx.scene.paint.Color.RED);
                                statusLabel.setText("Не удалось добавить трек.");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
                            statusLabel.setText("Ошибка при добавлении трека: " + ex.getMessage());
                        });
                        return null;
                    });
        } catch (NumberFormatException e) {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Ошибка: ID автора должен быть числом.");
        }
    }


    @FXML
    private void deleteSelectedTrack() {

        Track selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showError("Выберите трек для удаления.");
            return;
        }


        trackDAO.deleteTrack(selected.getId())
                .thenAccept(success -> {

                    Platform.runLater(() -> {
                        if (success) {
                            mainController.showAlert("Успех", "Трек успешно удален.");
                            loadTracksTable();


                            if (mainController != null) {
                                mainController.refreshTracks();
                            }

                        } else {

                            mainController.showError("Не удалось удалить трек. Проверьте ID или права доступа.");
                        }
                    });
                })
                .exceptionally(ex -> {

                    mainController.showError("Ошибка сети при удалении трека: " + ex.getMessage());
                    return null;
                });
    }
    private void clearTrackFields() {
        titleField.clear();
        if (artistField != null) artistField.clear();
        if (albumField != null) albumField.clear();
        if (genreField != null) genreField.clear();
        coverUrlField.clear();
        audioUrlField.clear();
        if (authorIdField != null) authorIdField.clear();
    }


    // =================================================================
    // ЛОГИКА ПОЛЬЗОВАТЕЛЕЙ
    // =================================================================

    private void loadUsersTable() {
        usersTable.getItems().clear();
        usersTable.setPlaceholder(new ProgressIndicator());

        adminDAO.getAllUsers()
                .thenAccept(users -> {
                    allUsers = users;
                    Platform.runLater(() -> {
                        usersTable.setPlaceholder(new Label("Нет данных о пользователях."));
                        usersTable.getItems().addAll(users);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        usersTable.setPlaceholder(new Label("Ошибка загрузки пользователей: " + ex.getMessage()));
                        mainController.showError("Ошибка загрузки пользователей: " + ex.getMessage());
                    });
                    return null;
                });
    }


    @FXML
    private void deleteSelectedUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showError("Выберите пользователя для удаления.");
            return;
        }

        adminDAO.deleteUser(selected.getId())
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            mainController.showAlert("Успех", "Пользователь успешно удален.");
                            loadUsersTable();
                        } else {
                            mainController.showError("Не удалось удалить пользователя.");
                        }
                    });
                })
                .exceptionally(ex -> {
                    mainController.showError("Ошибка сети при удалении пользователя: " + ex.getMessage());
                    return null;
                });
    }


    // =================================================================
    // ЛОГИКА АВТОРОВ
    // =================================================================

    private void loadAuthorsTable() {
        authorsTable.getItems().clear();
        authorsTable.setPlaceholder(new ProgressIndicator());

        authorDAO.getAllAuthors()
                .thenAccept(authors -> {
                    Platform.runLater(() -> {
                        authorsTable.setPlaceholder(new Label("Нет данных об авторах."));
                        authorsTable.getItems().addAll(authors);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        authorsTable.setPlaceholder(new Label("Ошибка загрузки авторов: " + ex.getMessage()));
                        mainController.showError("Ошибка загрузки авторов: " + ex.getMessage());
                    });
                    return null;
                });
    }



    @FXML
    public void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }

    @FXML
    private void addAuthor() {
        String name = authorNameField.getText();
        String photo = authorPhotoUrlField.getText();

        if (name.isEmpty()) {
            authorStatusLabel.setText("Имя автора обязательно.");
            return;
        }

        authorDAO.addAuthor(name, photo)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        authorStatusLabel.setText("Автор добавлен.");
                        authorNameField.clear();
                        authorPhotoUrlField.clear();
                        loadAuthorsTable();
                        loadAuthorsIntoPane();



                    } else {
                        authorStatusLabel.setText("Ошибка при добавлении автора.");
                    }
                }));
    }

    @FXML
    private void deleteSelectedAuthor() {
        Author selected = authorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showError("Выберите автора для удаления.");
            return;
        }

        authorDAO.deleteAuthor(selected.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        mainController.showAlert("Успех", "Автор удалён.");
                        loadAuthorsTable();
                        loadAuthorsIntoPane();

                    } else {
                        mainController.showError("Не удалось удалить автора.");
                    }
                }));
    }
    public void loadAuthorsIntoPane() {
        authorDAO.getAllAuthors().thenAccept(authors -> Platform.runLater(() -> {

            mainController.getAuthorsPane().getChildren().clear();


            for (Author author : authors) {
                Node authorCard = mainController.createAuthorCard(author.getName(), author.getPhotoUrl());
                mainController.getAuthorsPane().getChildren().add(authorCard);
            }
        }));
    }

    private void loadRadioTable() {
        radioTable.getItems().clear();
        radioTable.setPlaceholder(new ProgressIndicator());

        radioDAO.getAllStations()
                .thenAccept(stations -> Platform.runLater(() -> {
                    radioTable.setPlaceholder(
                            new Label("Нет радиостанций")
                    );
                    radioTable.getItems().addAll(stations);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            radioTable.setPlaceholder(
                                    new Label("Ошибка загрузки радио")
                            )
                    );
                    return null;
                });
    }

    @FXML
    private void addRadioStation() {
        String name = radioNameField.getText();
        String stream = radioStreamField.getText();
        String cover = radioCoverField.getText();

        if (name.isEmpty() || stream.isEmpty()) {
            radioStatusLabel.setText("Название и ссылка на поток обязательны");
            return;
        }

        radioDAO.addStation(name, stream, cover)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        radioStatusLabel.setText("Радиостанция добавлена");
                        radioNameField.clear();
                        radioStreamField.clear();
                        radioCoverField.clear();
                        loadRadioTable();
                        mainController.refreshRadios();
                    } else {
                        radioStatusLabel.setText("Ошибка добавления");
                    }
                }));
    }

    @FXML
    private void deleteSelectedRadio() {
        RadioStation selected =
                radioTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            mainController.showError("Выберите радиостанцию");
            return;
        }

        radioDAO.deleteStation(selected.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        mainController.showAlert("Успех", "Радиостанция удалена");
                        loadRadioTable();
                        mainController.refreshRadios();
                    } else {
                        mainController.showError("Не удалось удалить радиостанцию");
                    }
                }));
    }
    private void loadTracksForAlbumCreation() {
        trackDAO.getAllTracks()
                .thenAccept(tracks ->
                        Platform.runLater(() ->
                                albumTracksList.getItems().setAll(tracks)));
    }

    @FXML
    private void createAdminAlbum() {

        String title = albumTitleField.getText();
        String cover = albumCoverField.getText();

        List<Track> selected =
                albumTracksList.getSelectionModel()
                        .getSelectedItems();

        if (title.isEmpty() || selected.isEmpty()) {
            albumStatusLabel.setText("Заполните название и выберите треки");
            return;
        }

        adminPlaylistDAO.createAdminAlbum(title, cover, selected)
                .thenAccept(success ->
                        Platform.runLater(() -> {
                            if (success) {
                                albumStatusLabel.setText("Альбом создан");
                                albumTitleField.clear();
                                albumCoverField.clear();
                                albumTracksList.getSelectionModel().clearSelection();

                                // обновить список треков заново
                                loadTracksForAlbumCreation();

                                // обновить featured playlists на главном экране
                                if (mainController != null) {
                                    mainController.openFeaturedPlaylists();
                                }

                            } else {
                                albumStatusLabel.setText("Ошибка создания");
                            }
                        }));
    }

    @FXML
    private ComboBox<Playlist> existingAlbumsBox;
    private final PlaylistDAO playlistDAO = new PlaylistDAO();

    private void loadAlbumsBox() {
        playlistDAO.getFeaturedPlaylists()
                .thenAccept(playlists ->
                        Platform.runLater(() ->
                                existingAlbumsBox.getItems().setAll(playlists)
                        ));
    }

    @FXML
    private void addTrackToExistingAlbum() {
        Playlist selectedAlbum = existingAlbumsBox.getValue();
        Track selectedTrack = albumTracksList.getSelectionModel().getSelectedItem();

        if (selectedAlbum == null || selectedTrack == null) {
            albumStatusLabel.setText("Выберите альбом и трек");
            return;
        }

        playlistDAO.addTrackToPlaylist(selectedAlbum.getId(), selectedTrack.getId())
                .thenAccept(success ->
                        Platform.runLater(() -> {
                            if (success) {
                                albumStatusLabel.setText("Трек добавлен в альбом");
                            } else {
                                albumStatusLabel.setText("Ошибка добавления");
                            }
                        }));
    }

}