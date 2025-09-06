package com.filetransfer;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;

public class GamesController {
    @FXML
    private StackPane gamePane;

    private int tttXWins = 0;
    private int tttOWins = 0;
    private int tttDraws = 0;
    private int tttGames = 0;
    private boolean tttVsAI = false;

    @FXML
    private void openTicTacToe() {
        gamePane.getChildren().clear();
        VBox ttt = new VBox(15);
        ttt.setAlignment(Pos.CENTER);
        Label title = new Label("Tic Tac Toe");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");
        Label status = new Label("Player X's turn");
        status.setStyle("-fx-font-size: 16px; -fx-padding: 10 0 0 0;");
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);
        Button[][] cells = new Button[3][3];
        final String[] currentPlayer = { "X" };
        final boolean[] gameOver = { false };
        Button playAgainBtn = new Button("Play Again");
        playAgainBtn.setVisible(false);
        Button vsAIButton = new Button("Play vs Computer");
        vsAIButton.setStyle(
                "-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        Label stats = new Label();
        stats.setStyle("-fx-font-size: 14px; -fx-padding: 10 0 0 0;");
        Runnable updateStats = () -> stats.setText("X Wins: " + tttXWins + " | O Wins: " + tttOWins + " | Draws: "
                + tttDraws + " | Games: " + tttGames + (tttVsAI ? " | Mode: Vs Computer" : " | Mode: 2 Player"));
        updateStats.run();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button cell = new Button("");
                cell.setPrefSize(60, 60);
                cell.setStyle(
                        "-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-background-color: #e3f2fd;");
                final int row = i, col = j;
                cell.setOnAction(e -> {
                    if (!cell.getText().isEmpty() || gameOver[0])
                        return;
                    cell.setText(currentPlayer[0]);
                    if (checkWinHighlight(cells, currentPlayer[0])) {
                        status.setText("Player " + currentPlayer[0] + " wins!");
                        if (currentPlayer[0].equals("X"))
                            tttXWins++;
                        else
                            tttOWins++;
                        tttGames++;
                        gameOver[0] = true;
                        playAgainBtn.setVisible(true);
                        updateStats.run();
                    } else if (isDraw(cells)) {
                        status.setText("It's a draw!");
                        tttDraws++;
                        tttGames++;
                        gameOver[0] = true;
                        playAgainBtn.setVisible(true);
                        updateStats.run();
                    } else {
                        currentPlayer[0] = currentPlayer[0].equals("X") ? "O" : "X";
                        status.setText("Player " + currentPlayer[0] + "'s turn");
                        if (tttVsAI && currentPlayer[0].equals("O") && !gameOver[0]) {
                            aiMove(cells, currentPlayer, status, gameOver, playAgainBtn, updateStats);
                        }
                    }
                });
                cells[i][j] = cell;
                grid.add(cell, j, i);
            }
        }

        playAgainBtn.setOnAction(e -> {
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++) {
                    cells[i][j].setText("");
                    cells[i][j].setStyle(
                            "-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-background-color: #e3f2fd;");
                }
            currentPlayer[0] = "X";
            status.setText("Player X's turn");
            gameOver[0] = false;
            playAgainBtn.setVisible(false);
        });

        vsAIButton.setOnAction(e -> {
            tttVsAI = !tttVsAI;
            vsAIButton.setText(tttVsAI ? "Play 2 Player" : "Play vs Computer");
            playAgainBtn.fire();
            updateStats.run();
        });

        ttt.getChildren().addAll(title, grid, status, playAgainBtn, vsAIButton, stats);
        gamePane.getChildren().add(ttt);
    }

    private boolean checkWinHighlight(Button[][] cells, String player) {
        for (int i = 0; i < 3; i++) {
            if (cells[i][0].getText().equals(player) && cells[i][1].getText().equals(player)
                    && cells[i][2].getText().equals(player)) {
                for (int j = 0; j < 3; j++)
                    cells[i][j].setStyle(
                            "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 8;");
                return true;
            }
            if (cells[0][i].getText().equals(player) && cells[1][i].getText().equals(player)
                    && cells[2][i].getText().equals(player)) {
                for (int j = 0; j < 3; j++)
                    cells[j][i].setStyle(
                            "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 8;");
                return true;
            }
        }
        if (cells[0][0].getText().equals(player) && cells[1][1].getText().equals(player)
                && cells[2][2].getText().equals(player)) {
            for (int j = 0; j < 3; j++)
                cells[j][j].setStyle(
                        "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 8;");
            return true;
        }
        if (cells[0][2].getText().equals(player) && cells[1][1].getText().equals(player)
                && cells[2][0].getText().equals(player)) {
            for (int j = 0; j < 3; j++)
                cells[j][2 - j].setStyle(
                        "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 8;");
            return true;
        }
        return false;
    }

    private void aiMove(Button[][] cells, String[] currentPlayer, Label status, boolean[] gameOver, Button playAgainBtn,
            Runnable updateStats) {
        java.util.List<int[]> empty = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (cells[i][j].getText().isEmpty())
                    empty.add(new int[] { i, j });
        if (empty.isEmpty())
            return;
        int[] move = empty.get((int) (Math.random() * empty.size()));
        Button cell = cells[move[0]][move[1]];
        cell.setText("O");
        if (checkWinHighlight(cells, "O")) {
            status.setText("Player O wins!");
            tttOWins++;
            tttGames++;
            gameOver[0] = true;
            playAgainBtn.setVisible(true);
            updateStats.run();
        } else if (isDraw(cells)) {
            status.setText("It's a draw!");
            tttDraws++;
            tttGames++;
            gameOver[0] = true;
            playAgainBtn.setVisible(true);
            updateStats.run();
        } else {
            currentPlayer[0] = "X";
            status.setText("Player X's turn");
        }
    }

    @FXML
    private void openSnakeWaterGun() {
        gamePane.getChildren().clear();
        VBox swg = new VBox(15);
        swg.setAlignment(Pos.CENTER);
        Label title = new Label("Snake Water Gun");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");
        Label info = new Label("Choose your move:");
        HBox choices = new HBox(20);
        choices.setAlignment(Pos.CENTER);
        Button snakeBtn = new Button("Snake");
        Button waterBtn = new Button("Water");
        Button gunBtn = new Button("Gun");
        choices.getChildren().addAll(snakeBtn, waterBtn, gunBtn);
        Label result = new Label();
        result.setStyle("-fx-font-size: 16px; -fx-padding: 10 0 0 0;");
        Button playAgainBtn = new Button("Play Again");
        playAgainBtn.setVisible(false);

        String[] options = { "Snake", "Water", "Gun" };

        Runnable reset = () -> {
            result.setText("");
            playAgainBtn.setVisible(false);
            snakeBtn.setDisable(false);
            waterBtn.setDisable(false);
            gunBtn.setDisable(false);
        };

        java.util.function.Consumer<String> play = userChoice -> {
            String compChoice = options[(int) (Math.random() * 3)];
            String outcome;
            if (userChoice.equals(compChoice)) {
                outcome = "Draw! Both chose " + userChoice + ".";
            } else if ((userChoice.equals("Snake") && compChoice.equals("Water")) ||
                    (userChoice.equals("Water") && compChoice.equals("Gun")) ||
                    (userChoice.equals("Gun") && compChoice.equals("Snake"))) {
                outcome = "You Win! Computer chose " + compChoice + ".";
            } else {
                outcome = "You Lose! Computer chose " + compChoice + ".";
            }
            result.setText(outcome);
            playAgainBtn.setVisible(true);
            snakeBtn.setDisable(true);
            waterBtn.setDisable(true);
            gunBtn.setDisable(true);
        };

        snakeBtn.setOnAction(e -> play.accept("Snake"));
        waterBtn.setOnAction(e -> play.accept("Water"));
        gunBtn.setOnAction(e -> play.accept("Gun"));
        playAgainBtn.setOnAction(e -> reset.run());

        swg.getChildren().addAll(title, info, choices, result, playAgainBtn);
        gamePane.getChildren().add(swg);
    }

    @FXML
    private void openGuessNumber() {
        gamePane.getChildren().clear();
        VBox gn = new VBox(15);
        gn.setAlignment(Pos.CENTER);
        Label title = new Label("Guess the Number (1-100)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");
        Label info = new Label("I have a number. Can you guess it?");
        TextField guessField = new TextField();
        guessField.setPromptText("Enter your guess");
        guessField.setMaxWidth(150);
        Button guessBtn = new Button("Guess");
        Label result = new Label();
        result.setStyle("-fx-font-size: 16px; -fx-padding: 10 0 0 0;");
        Button playAgainBtn = new Button("Play Again");
        playAgainBtn.setVisible(false);

        final int[] number = { (int) (Math.random() * 100) + 1 };
        final int[] attempts = { 0 };

        guessBtn.setOnAction(e -> {
            String text = guessField.getText();
            if (!text.matches("\\d+")) {
                result.setText("Enter a valid number!");
                return;
            }
            int guess = Integer.parseInt(text);
            attempts[0]++;
            if (guess < number[0]) {
                result.setText("Too low!");
            } else if (guess > number[0]) {
                result.setText("Too high!");
            } else {
                result.setText("Correct! You guessed in " + attempts[0] + " tries.");
                guessBtn.setDisable(true);
                guessField.setDisable(true);
                playAgainBtn.setVisible(true);
            }
        });

        playAgainBtn.setOnAction(e -> {
            number[0] = (int) (Math.random() * 100) + 1;
            attempts[0] = 0;
            guessBtn.setDisable(false);
            guessField.setDisable(false);
            guessField.clear();
            result.setText("");
            playAgainBtn.setVisible(false);
        });

        gn.getChildren().addAll(title, info, guessField, guessBtn, result, playAgainBtn);
        gamePane.getChildren().add(gn);
    }

    private boolean isDraw(Button[][] cells) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (cells[i][j].getText().isEmpty())
                    return false;
        return true;
    }
}