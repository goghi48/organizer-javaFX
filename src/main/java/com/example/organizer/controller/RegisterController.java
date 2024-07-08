package com.example.organizer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            boolean userExists = DatabaseManager.checkUserExists(username);
            if (userExists) {
                errorLabel.setVisible(true);
                System.out.println("Username already exists.");
            } else {
                DatabaseManager.registerUser(username, password);
                System.out.println("User registered successfully.");
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("login-view.fxml"))));
                stage.setResizable(false);

                stage.show();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openLoginView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
