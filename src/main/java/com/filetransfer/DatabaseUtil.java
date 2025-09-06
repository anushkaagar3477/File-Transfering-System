package com.filetransfer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.filetransfer.FileTransferController.TransferRecord;

public class DatabaseUtil {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "filetransfer";
    private static final String URL = BASE_URL + DB_NAME;
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        // First try to connect to MySQL server without database
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD)) {
            // Create database if it doesn't exist
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }
        }

        // Now connect to the specific database
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Create users table
            String createUsersTable = """
                        CREATE TABLE IF NOT EXISTS users (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(50) UNIQUE NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            email VARCHAR(100) UNIQUE NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            last_ip VARCHAR(50)
                        )
                    """;

            // Create file_transfers table with receiver_id
            String createTransfersTable = """
                        CREATE TABLE IF NOT EXISTS file_transfers (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            sender_id INT,
                            receiver_id INT,
                            receiver_ip VARCHAR(50),
                            file_name VARCHAR(255),
                            file_size BIGINT,
                            transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            status VARCHAR(20),
                            FOREIGN KEY (sender_id) REFERENCES users(id),
                            FOREIGN KEY (receiver_id) REFERENCES users(id)
                        )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createUsersTable);
                System.out.println("Users table created/verified");
                stmt.execute(createTransfersTable);
                System.out.println("File transfers table created/verified");
                // Try to add receiver_id if not present
                try {
                    stmt.executeUpdate("ALTER TABLE file_transfers ADD COLUMN receiver_id INT AFTER sender_id");
                    stmt.executeUpdate(
                            "ALTER TABLE file_transfers ADD CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)");
                    System.out.println("receiver_id column added to file_transfers");
                } catch (SQLException ex) {
                    // Ignore if already exists
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database:");
            e.printStackTrace();
        }
    }

    public static boolean registerUser(String username, String password, String email) {
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, use proper password hashing
            pstmt.setString(3, email);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean validateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getUserId(String username) {
        String query = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if user not found
    }

    public static void logFileTransfer(Integer senderId, Integer receiverId, String receiverIp, String fileName,
            long fileSize, String status) {
        String query = "INSERT INTO file_transfers (sender_id, receiver_id, receiver_ip, file_name, file_size, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (senderId != null)
                pstmt.setInt(1, senderId);
            else
                pstmt.setNull(1, java.sql.Types.INTEGER);
            if (receiverId != null)
                pstmt.setInt(2, receiverId);
            else
                pstmt.setNull(2, java.sql.Types.INTEGER);
            pstmt.setString(3, receiverIp);
            pstmt.setString(4, fileName);
            pstmt.setLong(5, fileSize);
            pstmt.setString(6, status);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("File transfer logged to database. Rows affected: " + rowsAffected);
            System.out.println("Details - Sender ID: " + senderId + ", Receiver ID: " + receiverId + ", File: "
                    + fileName + ", Status: " + status);
        } catch (SQLException e) {
            System.err.println("Error logging file transfer to database:");
            e.printStackTrace();
        }
    }

    public static List<TransferRecord> getTransferHistory(int userId) {
        List<TransferRecord> history = new ArrayList<>();
        String query = "SELECT file_name, file_size, CASE WHEN sender_id = ? THEN 'Send' ELSE 'Receive' END AS transfer_type, status, transfer_date FROM file_transfers WHERE sender_id = ? OR receiver_id = ? ORDER BY transfer_date DESC";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String fileName = rs.getString("file_name");
                long fileSize = rs.getLong("file_size");
                String transferType = rs.getString("transfer_type");
                String status = rs.getString("status");
                Date date = rs.getTimestamp("transfer_date");
                TransferRecord record = new TransferRecord(
                        fileName,
                        formatFileSize(fileSize),
                        transferType,
                        status,
                        date);
                history.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transfer history:");
            e.printStackTrace();
        }
        return history;
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static int getUserIdByIp(String ip) {
        String query = "SELECT id FROM users WHERE last_ip = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, ip);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}