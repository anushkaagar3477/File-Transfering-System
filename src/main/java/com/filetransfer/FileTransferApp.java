package com.filetransfer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class FileTransferApp extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    private Stage primaryStage;

    @Override
    public void init() {
        DatabaseUtil.initializeDatabase();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        showLoginScene();
        primaryStage.show();
    }

    public void showLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
    }

    public void showSignupScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sign Up");
    }

    public void showFileTransferScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/filetransfer.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Transfer");
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
