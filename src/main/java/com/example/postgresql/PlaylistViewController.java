package com.example.postgresql;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import java.util.List;

public class PlaylistViewController {

    @FXML private Label playlistTitle;
    @FXML private VBox tracksContainer;

    private boolean canEdit = false;

    private Playlist playlist;
    private final PlaylistService service = new PlaylistService();
    private Form1 mainController;

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;

        boolean isAdmin = mainController.isAdmin();
        boolean isOwner = playlist.getOwnerId() == mainController.getCurrentUserId();
        boolean isFeatured = playlist.isFeatured();

        canEdit = isAdmin || (!isFeatured && isOwner);

        playlistTitle.setText(playlist.getTitle());
        loadTracks();
    }
    private List<Track> currentPlaylistTracks;

    private void loadTracks() {
        service.loadPlaylistTracks(playlist.getId())
                .thenAccept(tracks -> Platform.runLater(() -> {
                    tracksContainer.getChildren().clear();

                    for (Track t : tracks) {


                        tracksContainer.getChildren().add(
                                TrackCardFactory.create(
                                        t,
                                        this::openTrack,
                                        canEdit ? this::removeTrack : null
                                )
                        );
                    }

                    this.currentPlaylistTracks = tracks;
                }));
    }

    private void openTrack(Track track) {
        mainController.openTrackDetail(track, currentPlaylistTracks);
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
    private void removeTrack(Track track) {
        service.removeTrack(playlist.getId(), track.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        loadTracks();
                    } else {
                        mainController.showError("Не удалось удалить трек");
                    }
                }));
    }
}