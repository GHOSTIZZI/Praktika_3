package com.example.postgresql;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.scene.layout.FlowPane;

public class TrackCatalogController {


    @FXML
    private FlowPane fullTrackFlowPane;

    private Form1 mainController;

    private final TrackDAO trackDAO = new TrackDAO();

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }


    public void loadAllTracks() {
        fullTrackFlowPane.getChildren().clear();
        fullTrackFlowPane.setPrefWrapLength(1820);

        Label loadingLabel = new Label("Загрузка каталога");
        fullTrackFlowPane.getChildren().add(loadingLabel);


        trackDAO.getAllTracks()
                .thenAccept(allTracks -> {

                    Platform.runLater(() -> {
                        fullTrackFlowPane.getChildren().clear();

                        if (allTracks.isEmpty()) {
                            fullTrackFlowPane.getChildren().add(new Label("Каталог пуст."));
                        } else {
                            for (Track track : allTracks) {
                                fullTrackFlowPane.getChildren().add(mainController.createMusicCard(track));
                            }
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        fullTrackFlowPane.getChildren().clear();
                        fullTrackFlowPane.getChildren().add(new Label("Ошибка загрузки каталога: " + ex.getMessage()));
                    });
                    return null;
                });
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}