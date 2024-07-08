package com.example.organizer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        try {
            DatabaseManager.getConnection();
            System.out.println("aboba");
        } catch (SQLException e) {
            System.err.println("zxc");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            int userId = DatabaseManager.authenticateUser(username, password);
            if (userId != -1) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("organizer-view.fxml"));
                Parent root = loader.load();

                Controller controller = loader.getController();
                controller.setUserId(userId);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle("Organizer");
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                errorLabel.setVisible(true);
                System.out.println("Incorrect username or password.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openRegisterView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("register-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Registration");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
