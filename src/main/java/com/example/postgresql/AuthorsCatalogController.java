package com.example.postgresql;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.concurrent.CancellationException;

public class AuthorsCatalogController {

    @FXML
    private FlowPane fullAuthorsFlowPane;

    private Form1 mainController;

    private final AuthorDAO authorDAO = new AuthorDAO();

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }


    public void loadAllAuthors() {
        fullAuthorsFlowPane.getChildren().clear();
        fullAuthorsFlowPane.setPrefWrapLength(1820);

        Label loadingLabel = new Label("Загрузка авторов");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        fullAuthorsFlowPane.getChildren().add(loadingLabel);


        authorDAO.getAllAuthors()
                .thenAccept((List<Author> authors) -> {

                    Platform.runLater(() -> {
                        fullAuthorsFlowPane.getChildren().clear();

                        if (authors.isEmpty()) {
                            fullAuthorsFlowPane.getChildren().add(new Label("Нет доступных авторов"));
                        } else {

                            for (Author author : authors) {

                                VBox authorCard = mainController.createAuthorCard(author.getName(), author.getPhotoUrl());
                                fullAuthorsFlowPane.getChildren().add(authorCard);
                            }
                        }
                    });
                })
                .exceptionally((Throwable ex) -> {

                    Platform.runLater(() -> {
                        fullAuthorsFlowPane.getChildren().clear();


                        if (!(ex.getCause() instanceof CancellationException)) {
                            fullAuthorsFlowPane.getChildren().add(new Label("Ошибка загрузки авторов: " + ex.getMessage()));
                            ex.printStackTrace();
                        }
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
}