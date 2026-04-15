package com.example.postgresql;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;

public class AccController {

    @FXML
    private Label loginLabel;

    @FXML
    private ImageView avatarImage;

    private Form1 mainController;

    @FXML
    private ImageView logoutIcon;

    @FXML
    public void initialize() {
        Image image = new Image(getClass().getResourceAsStream("/images/logout.png"));
        logoutIcon.setImage(image);
    }

    public void setMainController(Form1 controller) {
        this.mainController = controller;

        loadAccountInfo();
    }

    private void loadAccountInfo() {

        loadAvatarImage();

        if (mainController != null) {
            String login = mainController.getCurrentUserLogin();

            if (login != null) {
                loginLabel.setText(login);
            } else {
                loginLabel.setText("Гость");
            }
        }
    }

    private void loadAvatarImage() {
        try {

            Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/default_avatar.png"));
            avatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения аккаунта: " + e.getMessage());

            avatarImage.setImage(new Image("https://via.placeholder.com/100x100/333333/ffffff?text=User"));
        }
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            if (mainController != null) {
                mainController.stopCurrentTrack();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) avatarImage.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}