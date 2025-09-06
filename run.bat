@echo off
echo Building and running File Transfer Application...

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Java is not installed or not in PATH
    echo Please install Java and try again
    pause
    exit /b 1
)

REM Set JavaFX module path
set JAVAFX_PATH=javafx-sdk-24.0.1\lib
set MODULE_PATH=%JAVAFX_PATH%\javafx.base.jar;%JAVAFX_PATH%\javafx.controls.jar;%JAVAFX_PATH%\javafx.fxml.jar;%JAVAFX_PATH%\javafx.graphics.jar;%JAVAFX_PATH%\javafx.media.jar;%JAVAFX_PATH%\javafx.swing.jar;%JAVAFX_PATH%\javafx.web.jar

REM Set MySQL connector path
set MYSQL_PATH=mysql-connector-j-8.3.0\mysql-connector-j-8.3.0.jar

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile the Java files
echo Compiling Java files...
javac -d bin --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "%MYSQL_PATH%" src\main\java\com\filetransfer\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed
    pause
    exit /b 1
)

REM Copy resources (FXML, CSS) to bin directory
xcopy /E /Y src\main\resources\* bin\

REM Run the application
echo Starting application...
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "bin;%MYSQL_PATH%" com.filetransfer.FileTransferApp

if %ERRORLEVEL% neq 0 (
    echo Application failed to start
    pause
    exit /b 1
)

pause 