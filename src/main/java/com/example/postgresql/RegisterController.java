package com.example.postgresql;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;


public class RegisterController {

    @FXML private TextField newUsername;
    @FXML private PasswordField newPassword;
    @FXML private Label registerStatusLabel;


    private final AuthDAO authDAO = new AuthDAO();

    @FXML
    private void createAccount(ActionEvent event) {
        String username = newUsername.getText().trim();
        String password = newPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            registerStatusLabel.setText("Введите логин и пароль!");
            return;
        }

        if (password.length() < 8) {
            registerStatusLabel.setText("Пароль должен быть не менее 8 символов!");
            return;
        }

        registerStatusLabel.setText("Регистрация");


        authDAO.register(username, password)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            registerStatusLabel.setText("Регистрация успешна!");
                            openLoginForm(event);
                        } else {

                            registerStatusLabel.setText("Ошибка регистрации. Пользователь уже существует или ошибка сервера.");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        registerStatusLabel.setText("Сетевая ошибка при регистрации: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                    return null;
                });
    }


    @FXML
    private void openLoginForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}