package com.example.postgresql;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class TrackCardFactory {

    public static VBox create(
            Track track,
            Consumer<Track> onOpen,
            Consumer<Track> onDelete
    ) {

        VBox card = new VBox();
        card.setSpacing(6);
        card.setStyle(
                "-fx-background-color:#1a1a1a;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:10;" +
                        "-fx-cursor:hand;"
        );

        ImageView cover = new ImageView();
        cover.setFitWidth(80);
        cover.setFitHeight(80);

        if (track.getCoverUrl() != null && !track.getCoverUrl().isEmpty()) {
            try {
                cover.setImage(new Image(track.getCoverUrl(), true));
            } catch (Exception ignored) {}
        }

        Label title = new Label(track.getTitle());
        title.setStyle("-fx-text-fill:white; -fx-font-size:14px;");

        Label artist = new Label(track.getArtist());
        artist.setStyle("-fx-text-fill:#b3b3b3; -fx-font-size:12px;");

        VBox info = new VBox(title, artist);

        ImageView deleteBtn = null;

        if (onDelete != null) {
            Image deleteIcon = new Image(
                    TrackCardFactory.class
                            .getResource("/images/delete.png")
                            .toExternalForm()
            );

            deleteBtn = new ImageView(deleteIcon);
            deleteBtn.setFitWidth(25);
            deleteBtn.setFitHeight(25);
            deleteBtn.setStyle("-fx-cursor: hand;");

            deleteBtn.setOnMouseClicked(e -> {
                e.consume();
                onDelete.accept(track);
            });
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row;

        if (deleteBtn != null) {
            row = new HBox(20, cover, info, spacer, deleteBtn);
        } else {
            row = new HBox(20, cover, info, spacer);
        }
        card.getChildren().add(row);
        card.setOnMouseClicked(e -> onOpen.accept(track));
        return card;
    }
}