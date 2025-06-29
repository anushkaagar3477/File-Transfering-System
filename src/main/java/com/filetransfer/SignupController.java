package com.filetransfer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SignupController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Remove all custom window control fields and logic
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Invalid email format");
            return;
        }

        // Attempt to register user
        if (DatabaseUtil.registerUser(username, password, email)) {
            try {
                // Load the login view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                
                // Get the current stage
                Stage stage = (Stage) usernameField.getScene().getWindow();
                
                // Create new scene
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
                
                // Set the new scene
                stage.setScene(scene);
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Error loading login view");
            }
        } else {
            errorLabel.setText("Username or email already exists");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            // Create new scene
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            // Set the new scene
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading login view");
        }
    }
} 