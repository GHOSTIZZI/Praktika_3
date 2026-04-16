package com.example.postgresql;

import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.TextField;
import javafx.application.Platform;


public class PlaylistController {

    @FXML private FlowPane playlistsPane;
    @FXML private TextField playlistTitleField;


    private final PlaylistService playlistService = new PlaylistService();
    private Form1 mainController;

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public void loadUserPlaylists(int userId) {
        playlistService.loadUserPlaylists(userId)
                .thenAccept(playlists -> Platform.runLater(() ->
                        PlaylistRenderer.render(
                                playlistsPane,
                                playlists,
                                this::openPlaylist,
                                this::deletePlaylist
                        )
                ));
    }

    private void deletePlaylist(Playlist playlist) {
        playlistService.deletePlaylist(playlist.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        loadUserPlaylists(mainController.getCurrentUserId());
                    } else {
                        mainController.showError("Не удалось удалить плейлист");
                    }
                }));
    }

    @FXML
    private void createPlaylist() {
        int userId = mainController.getCurrentUserId();

        playlistService.createPlaylist(
                playlistTitleField.getText(),
                null,
                userId
        ).thenAccept(success -> {
            if (success) loadUserPlaylists(userId);
        });
    }

    private void openPlaylist(Playlist playlist) {
        if (mainController != null) {
            mainController.openPlaylistView(playlist);
        }
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }

}