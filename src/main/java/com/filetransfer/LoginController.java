package com.filetransfer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.HBox;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        if (DatabaseUtil.validateUser(username, password)) {
            try {
                // Get user ID
                int userId = DatabaseUtil.getUserId(username);
                if (userId == -1) {
                    errorLabel.setText("Error retrieving user information");
                    return;
                }

                // Load the main file transfer view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/filetransfer.fxml"));
                Parent root = loader.load();
                
                // Get the controller and set the user ID
                FileTransferController controller = loader.getController();
                controller.setUserId(userId);
                
                // Get the current stage
                Stage stage = (Stage) usernameField.getScene().getWindow();
                
                // Create new scene
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
                
                // Set the new scene
                stage.setScene(scene);
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Error loading main view");
            }
        } else {
            errorLabel.setText("Invalid username or password");
        }
    }

    @FXML
    private void handleSignup() {
        try {
            // Load the signup view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
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
            errorLabel.setText("Error loading signup view");
        }
    }
} 