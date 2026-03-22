package io.bino.fx;

import io.bino.core.logic.SudokuGenerator;
import io.bino.core.logic.SudokuSolver;
import io.bino.core.model.Difficulty;
import io.bino.core.model.GameState;
import io.bino.core.model.SudokuBoard;
import io.bino.fx.component.BoardGrid;
import io.bino.fx.component.ControlBar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Sudoku application.
 * Mirrors the Swing UI in layout and functionality, using JavaFX components and CSS.
 */
public class SudokuFxApp extends Application {

    private final SudokuGenerator generator = new SudokuGenerator();
    private final SudokuSolver solver = new SudokuSolver();

    private GameState gameState;
    private BoardGrid boardGrid;
    private ControlBar controlBar;
    private BorderPane root;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        root = new BorderPane();

        startNewGame(Difficulty.EASY);

        Scene scene = new Scene(root, 560, 680);
        String css = getClass().getResource("sudoku.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Sudoku (JavaFX)");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(createAppIcon(32));
        stage.getIcons().add(createAppIcon(16));
        stage.show();
    }

    private void startNewGame(Difficulty difficulty) {
        if (gameState != null) {
            gameState.pauseTimer();
        }

        SudokuBoard puzzle = generator.generate(difficulty);
        gameState = new GameState(puzzle, difficulty);

        boardGrid = new BoardGrid(gameState);
        gameState.addGameEventListener(boardGrid);

        if (controlBar == null) {
            controlBar = new ControlBar(
                    gameState,
                    this::startNewGame,
                    this::applyHint,
                    this::solvePuzzle,
                    this::resetPuzzle
            );
        } else {
            controlBar.rebind(gameState);
        }
        gameState.addGameEventListener(controlBar);
        // Listen for PUZZLE_COMPLETE
        gameState.addGameEventListener(event -> {
            if (event.getType() == io.bino.core.event.GameEvent.Type.PUZZLE_COMPLETE) {
                Platform.runLater(this::showCompletion);
            }
        });

        StackPane boardWrapper = new StackPane(boardGrid);
        boardWrapper.setPadding(new Insets(15));
        boardWrapper.setAlignment(Pos.CENTER);

        root.setCenter(boardWrapper);
        root.setBottom(controlBar);

        gameState.startTimer();
    }

    private void applyHint() {
        gameState.applyHint();
    }

    private void solvePuzzle() {
        SudokuBoard copy = gameState.getBoard().copy();
        if (solver.solve(copy)) {
            int[][] arr = copy.toArray();
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (!gameState.getBoard().isGiven(r, c)) {
                        int existing = gameState.getBoard().getValue(r, c);
                        if (existing != arr[r][c]) {
                            gameState.enterValue(r, c, arr[r][c]);
                        }
                    }
                }
            }
        }
    }

    private void resetPuzzle() {
        gameState.getBoard().clearUserCells();
        gameState.resetTimer();
        gameState.startTimer();
        gameState.fireEvent(new io.bino.core.event.GameEvent(
                io.bino.core.event.GameEvent.Type.CELL_CHANGED));
    }

    private void showCompletion() {
        long seconds = gameState.getElapsedSeconds();
        int mistakes = gameState.getMistakeCount();
        String time = String.format("%d:%02d", seconds / 60, seconds % 60);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations");
        alert.setHeaderText("Puzzle solved!");
        alert.setContentText("Time: " + time + "  |  Mistakes: " + mistakes);
        alert.initOwner(primaryStage);
        alert.show();
    }

    /** Renders the app icon programmatically — a 3×3 Sudoku grid with a highlighted centre cell. */
    private static Image createAppIcon(int size) {
        // Delegate to AWT for rendering, then convert to JavaFX WritableImage
        java.awt.image.BufferedImage buf =
                new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = buf.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                           java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                           java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(new java.awt.Color(0x1a2744));
        g.fillRoundRect(0, 0, size, size, size / 8, size / 8);

        int pad = size / 16;
        int gap = size / 16;
        int cellSize = (size - 2 * pad - 2 * gap) / 3;
        int[][] digits = {{5, 3, 7}, {6, 9, 2}, {1, 8, 4}};
        java.awt.Color dimCell    = new java.awt.Color(0x26, 0x3a, 0x66, 180);
        java.awt.Color highlight  = new java.awt.Color(0x4a, 0x90, 0xd9, 255);
        java.awt.Color dimText    = new java.awt.Color(0x7a, 0x9f, 0xd4, 255);
        java.awt.Color centreText = java.awt.Color.WHITE;
        java.awt.Font font = new java.awt.Font("Arial", java.awt.Font.BOLD,
                                               Math.max(6, cellSize * 7 / 10));
        g.setFont(font);
        var fm = g.getFontMetrics();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = pad + col * (cellSize + gap);
                int y = pad + row * (cellSize + gap);
                boolean centre = row == 1 && col == 1;
                g.setColor(centre ? highlight : dimCell);
                g.fillRoundRect(x, y, cellSize, cellSize, 2, 2);
                g.setColor(centre ? centreText : dimText);
                String d = String.valueOf(digits[row][col]);
                g.drawString(d, x + (cellSize - fm.stringWidth(d)) / 2,
                                y + (cellSize + fm.getAscent() - fm.getDescent()) / 2);
            }
        }
        g.dispose();

        WritableImage img = new WritableImage(size, size);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int argb = buf.getRGB(x, y);
                pw.setArgb(x, y, argb);
            }
        }
        return img;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
