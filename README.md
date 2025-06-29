# File Transfer Application

A modern JavaFX application for secure file transfer between users with MySQL database integration.

## Features

- User authentication (login/signup)
- Secure file transfer
- Real-time transfer progress
- Transfer history tracking
- Modern UI with animations
- MySQL database integration

## Prerequisites

- Java 17 or higher
- MySQL Server
- Maven

## Setup

1. Create a MySQL database named `filetransfer`:
```sql
CREATE DATABASE filetransfer;
```

2. Update database credentials in `DatabaseUtil.java` if needed:
```java
private static final String URL = "jdbc:mysql://localhost:3306/filetransfer";
private static final String USER = "root";
private static final String PASSWORD = "";
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn javafx:run
```

## Usage

1. Launch the application
2. Create a new account or login with existing credentials
3. To send a file:
   - Click "Select File" to choose a file
   - Enter the receiver's IP address
   - Click "Send File"
4. To receive a file:
   - Click "Start Receiving"
   - Wait for incoming files
   - Click "Stop Receiving" when done

## Project Structure

- `src/main/java/com/filetransfer/`
  - `FileTransferApp.java` - Main application class
  - `DatabaseUtil.java` - Database operations
  - `LoginController.java` - Login view controller
  - `SignupController.java` - Signup view controller
  - `FileTransferController.java` - File transfer operations
- `src/main/resources/fxml/`
  - `login.fxml` - Login view
  - `signup.fxml` - Signup view
  - `filetransfer.fxml` - File transfer view
- `src/main/resources/styles/`
  - `main.css` - Application styling

## Security Notes

- The application uses basic authentication
- File transfers are done over TCP/IP
- In a production environment, implement proper password hashing and encryption 