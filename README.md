# ByteRace

A modern JavaFX application for secure file transfer between users with MySQL database integration, now featuring built-in mini-games for enhanced user experience.

## Features

- User authentication (login/signup)
- Secure file transfer over TCP/IP
- Real-time transfer progress
- Transfer history tracking
- Modern UI with animations and improved UX
- MySQL database integration
- **Integrated Mini-Games:**
  - Tic Tac Toe (2-player and vs Computer)
  - Snake Water Gun
  - Guess the Number
- Easy logout and session management

## Prerequisites

- Java 17 or higher
- MySQL Server

## Setup

1. Create a MySQL database named `filetransfer`:
```sql
CREATE DATABASE filetransfer;
```

2. Update database credentials in `DatabaseUtil.java` if needed:
```java
private static final String URL = "jdbc:mysql://localhost:3306/filetransfer";
private static final String USER = "root";
private static final String PASSWORD = "root";
```

3. Build and run the project using the provided batch script:
```bat
run.bat
```

   - This script will compile the Java files, copy resources, and launch the application.
   - Make sure your MySQL server is running before starting the application.

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
5. To play games:
   - Click the "Games" button on the main screen
   - Choose from Tic Tac Toe, Snake Water Gun, or Guess the Number

## Project Structure

- `src/main/java/com/filetransfer/`
  - `FileTransferApp.java` - Main application class
  - `DatabaseUtil.java` - Database operations
  - `LoginController.java` - Login view controller
  - `SignupController.java` - Signup view controller
  - `FileTransferController.java` - File transfer operations
  - `GamesController.java` - Mini-games logic and UI
- `src/main/resources/fxml/`
  - `login.fxml` - Login view
  - `signup.fxml` - Signup view
  - `filetransfer.fxml` - File transfer view
  - `games.fxml` - Games view
- `src/main/resources/styles/`
  - `main.css` - Application styling

## Security Notes

- The application uses basic authentication
- File transfers are done over TCP/IP
- **In a production environment, implement proper password hashing and encryption** 