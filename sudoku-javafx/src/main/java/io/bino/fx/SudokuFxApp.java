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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
        var iconUrl = getClass().getResource("icon.svg");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }
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

    public static void main(String[] args) {
        launch(args);
    }
}
