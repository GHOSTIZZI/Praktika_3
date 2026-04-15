package com.example.postgresql;
import javafx.scene.layout.FlowPane;
import java.util.List;
import java.util.function.Consumer;
public class PlaylistRenderer {
    public static void render(
            FlowPane container,
            List<Playlist> playlists,
            Consumer<Playlist> onOpen,
            Consumer<Playlist> onDelete
    ) {
        container.getChildren().clear();

        for (Playlist p : playlists) {
            container.getChildren().add(PlaylistCardFactory.create(p, onOpen, onDelete));
        }
    }
}