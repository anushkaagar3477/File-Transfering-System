package com.filetransfer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Date;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class FileTransferController {
    @FXML
    private TextField receiverIpField;
    @FXML
    private Button selectFileButton;
    @FXML
    private Button sendFileButton;
    @FXML
    private Button startReceivingButton;
    @FXML
    private Button stopReceivingButton;
    @FXML
    private ProgressBar sendProgressBar;
    @FXML
    private ProgressBar receiveProgressBar;
    @FXML
    private Label sendStatusLabel;
    @FXML
    private Label receiveStatusLabel;
    @FXML
    private TableView<TransferRecord> transferHistoryTable;
    @FXML
    private TableColumn<TransferRecord, String> fileNameColumn;
    @FXML
    private TableColumn<TransferRecord, String> fileSizeColumn;
    @FXML
    private TableColumn<TransferRecord, String> transferTypeColumn;
    @FXML
    private TableColumn<TransferRecord, String> statusColumn;
    @FXML
    private TableColumn<TransferRecord, Date> dateColumn;
    @FXML
    private Button logoutButton;
    @FXML
    private Button gamesButton;

    private File selectedFile;
    private ServerSocket serverSocket;
    private boolean isReceiving = false;
    private ObservableList<TransferRecord> transferHistory = FXCollections.observableArrayList();
    private int userId = -1;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("User ID set to: " + userId);
        try {
            String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
            try (Connection conn = DatabaseUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET last_ip = ? WHERE id = ?")) {
                pstmt.setString(1, localIp);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Could not update last_ip for user: " + e.getMessage());
        }
        transferHistory.clear();
        transferHistory.addAll(DatabaseUtil.getTransferHistory(userId));
    }

    @FXML
    public void initialize() {
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        transferTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transferType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        transferHistoryTable.setItems(transferHistory);
    }

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        selectedFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());

        if (selectedFile != null) {
            sendFileButton.setDisable(false);
            sendStatusLabel.setText("File selected: " + selectedFile.getName());
        }
    }

    @FXML
    private void handleSendFile() {
        if (selectedFile == null || receiverIpField.getText().isEmpty()) {
            sendStatusLabel.setText("Please select a file and enter receiver IP");
            return;
        }

        new Thread(() -> {
            try {
                Socket socket = new Socket(receiverIpField.getText(), 5000);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                // Send file name and size
                dos.writeUTF(selectedFile.getName());
                dos.writeLong(selectedFile.length());

                // Send file data
                FileInputStream fis = new FileInputStream(selectedFile);
                byte[] buffer = new byte[4096];
                long totalBytesRead = 0;
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    double progress = (double) totalBytesRead / selectedFile.length();
                    sendProgressBar.setProgress(progress);
                }

                fis.close();
                dos.close();
                socket.close();

                sendStatusLabel.setText("File sent successfully");
                TransferRecord record = new TransferRecord(
                        selectedFile.getName(),
                        formatFileSize(selectedFile.length()),
                        "Send",
                        "Completed",
                        new Date());
                transferHistory.add(0, record);

                Integer receiverId = DatabaseUtil.getUserIdByIp(receiverIpField.getText());
                DatabaseUtil.logFileTransfer(
                        getCurrentUserId(),
                        receiverId != -1 ? receiverId : null,
                        receiverIpField.getText(),
                        selectedFile.getName(),
                        selectedFile.length(),
                        "Completed");

                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("File Transfer");
                    alert.setHeaderText(null);
                    alert.setContentText("File sent successfully!");
                    alert.showAndWait();
                });

            } catch (Exception e) {
                e.printStackTrace();
                sendStatusLabel.setText("Error sending file: " + e.getMessage());
                System.err.println("Error sending file: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleStartReceiving() {
        if (isReceiving)
            return;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);
                isReceiving = true;
                Platform.runLater(() -> {
                    startReceivingButton.setDisable(true);
                    stopReceivingButton.setDisable(false);
                    receiveStatusLabel.setText("Waiting for incoming files...");
                });

                while (isReceiving) {
                    Socket socket = serverSocket.accept();
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();
                    String senderIp = socket.getInetAddress().getHostAddress();

                    final File[] saveFile = new File[1];
                    CountDownLatch latch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save File");
                        fileChooser.setInitialFileName(fileName);
                        saveFile[0] = fileChooser.showSaveDialog(startReceivingButton.getScene().getWindow());
                        latch.countDown();
                    });
                    latch.await();

                    if (saveFile[0] != null) {
                        FileOutputStream fos = new FileOutputStream(saveFile[0]);
                        byte[] buffer = new byte[4096];
                        long totalBytesRead = 0;
                        int bytesRead;

                        while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            double progress = (double) totalBytesRead / fileSize;
                            final double prog = progress;
                            Platform.runLater(() -> receiveProgressBar.setProgress(prog));
                        }

                        fos.close();
                        dis.close();
                        socket.close();

                        try {
                            String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
                            try (Connection conn = DatabaseUtil.getConnection();
                                    PreparedStatement pstmt = conn
                                            .prepareStatement("UPDATE users SET last_ip = ? WHERE id = ?")) {
                                pstmt.setString(1, localIp);
                                pstmt.setInt(2, getCurrentUserId());
                                pstmt.executeUpdate();
                            }
                        } catch (Exception e) {
                            System.err.println("Could not update last_ip for receiver: " + e.getMessage());
                        }

                        Platform.runLater(() -> {
                            receiveStatusLabel.setText("File received: " + fileName);
                            TransferRecord record = new TransferRecord(
                                    fileName,
                                    formatFileSize(fileSize),
                                    "Receive",
                                    "Completed",
                                    new Date());
                            transferHistory.add(0, record);

                            Integer senderId = DatabaseUtil.getUserIdByIp(senderIp);
                            DatabaseUtil.logFileTransfer(
                                    senderId != -1 ? senderId : null,
                                    getCurrentUserId(),
                                    senderIp,
                                    fileName,
                                    fileSize,
                                    "Completed");

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("File Transfer");
                            alert.setHeaderText(null);
                            alert.setContentText("File received successfully!");
                            alert.showAndWait();
                        });
                    } else {
                        dis.close();
                        socket.close();
                        Platform.runLater(() -> receiveStatusLabel.setText("File save cancelled."));
                    }
                }
            } catch (Exception e) {
                if (isReceiving) {
                    e.printStackTrace();
                    Platform.runLater(() -> receiveStatusLabel.setText("Error receiving file: " + e.getMessage()));
                }
            }
        }).start();
    }

    @FXML
    private void handleStopReceiving() {
        isReceiving = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        startReceivingButton.setDisable(false);
        stopReceivingButton.setDisable(true);
        receiveStatusLabel.setText("Receiving stopped");
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGames() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/games.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            Stage gameStage = new Stage();
            gameStage.setTitle("Games");
            gameStage.setScene(scene);
            gameStage.initOwner(gamesButton.getScene().getWindow());
            gameStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private int getCurrentUserId() {
        if (userId == -1) {
            System.err.println("Warning: User ID not set. Using default value of 1.");
            return 1;
        }
        System.out.println("Using user ID: " + userId);
        return userId;
    }

    public static class TransferRecord {
        private final String fileName;
        private final String fileSize;
        private final String transferType;
        private final String status;
        private final Date date;

        public TransferRecord(String fileName, String fileSize, String transferType, String status, Date date) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.transferType = transferType;
            this.status = status;
            this.date = date;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileSize() {
            return fileSize;
        }

        public String getTransferType() {
            return transferType;
        }

        public String getStatus() {
            return status;
        }

        public Date getDate() {
            return date;
        }
    }
}