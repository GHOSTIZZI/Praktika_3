package com.example.postgresql;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class PlaylistCardFactory {

    private static final Image DEFAULT_COVER =
            new Image(PlaylistCardFactory.class
                    .getResource("/images/default_playlist.jpg")
                    .toExternalForm());

    public static VBox create(
            Playlist playlist,
            Consumer<Playlist> onOpen,
            Consumer<Playlist> onDelete
    ) {

        VBox card = new VBox(8);
        card.setStyle("""
            -fx-background-color: #1e1e1e;
            -fx-background-radius: 10;
            -fx-padding: 10;
        """);
        card.setMaxWidth(160);

        Image cover;

        if (playlist.getCoverUrl() != null &&
                !playlist.getCoverUrl().trim().isEmpty()) {

            cover = new Image(
                    playlist.getCoverUrl(),
                    true
            );
        } else {
            cover = DEFAULT_COVER;
        }

        ImageView coverView = new ImageView(cover);
        coverView.setFitWidth(180);
        coverView.setFitHeight(240);
        coverView.setPreserveRatio(true);


        Label title = new Label(playlist.getTitle());
        title.setStyle("-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold;");


        Label subtitle = new Label("Плейлист");
        subtitle.setStyle("-fx-text-fill:#888; -fx-font-size:12px;");

        Image deleteIcon = new Image(
                PlaylistCardFactory.class
                        .getResource("/images/delete.png")
                        .toExternalForm()
        );

        ImageView deleteIconView = new ImageView(deleteIcon);
        deleteIconView.setFitWidth(25);
        deleteIconView.setFitHeight(25);



        Button deleteBtn = new Button();
        deleteBtn.setGraphic(deleteIconView);
        deleteBtn.setStyle("-fx-background-color: transparent;");

        if (onDelete != null) {
            deleteBtn.setOnAction(e -> onDelete.accept(playlist));
        } else {
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }

        if (onDelete != null) {
            deleteBtn.setOnAction(e -> onDelete.accept(playlist));
        } else {
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, title, spacer, deleteBtn);


        card.setOnMouseClicked(e -> onOpen.accept(playlist));
        card.getChildren().addAll(coverView, header, subtitle);

        return card;
    }
}