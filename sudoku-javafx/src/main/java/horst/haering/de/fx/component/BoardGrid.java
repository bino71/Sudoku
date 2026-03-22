package horst.haering.de.fx.component;

import horst.haering.de.core.event.GameEvent;
import horst.haering.de.core.event.GameEventListener;
import horst.haering.de.core.logic.SudokuValidator;
import horst.haering.de.core.model.CellState;
import horst.haering.de.core.model.GameState;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import java.util.Set;

/**
 * 9x9 GridPane of TextFields representing the Sudoku board.
 * Implements GameEventListener to update cell styles on model changes.
 */
public class BoardGrid extends GridPane implements GameEventListener {

    private static final int CELL_SIZE = 56;

    private final TextField[][] cells = new TextField[9][9];
    private final GameState gameState;
    private final SudokuValidator validator = new SudokuValidator();

    private int lastHintRow = -1, lastHintCol = -1;

    public BoardGrid(GameState gameState) {
        this.gameState = gameState;
        setMinSize(CELL_SIZE * 9, CELL_SIZE * 9);
        setMaxSize(CELL_SIZE * 9, CELL_SIZE * 9);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                TextField tf = createCell(r, c);
                cells[r][c] = tf;
                add(tf, c, r);
            }
        }
    }

    private TextField createCell(int row, int col) {
        TextField tf = new TextField();
        tf.setPrefSize(CELL_SIZE, CELL_SIZE);
        tf.setMinSize(CELL_SIZE, CELL_SIZE);
        tf.setMaxSize(CELL_SIZE, CELL_SIZE);
        tf.getStyleClass().add("sudoku-cell");

        // Apply box border CSS classes
        boolean isBoxRight  = (col == 2 || col == 5);
        boolean isBoxBottom = (row == 2 || row == 5);
        if (isBoxRight && isBoxBottom) {
            tf.getStyleClass().add("box-border-corner");
        } else if (isBoxRight) {
            tf.getStyleClass().add("box-border-right");
        } else if (isBoxBottom) {
            tf.getStyleClass().add("box-border-bottom");
        }

        CellState state = gameState.getBoard().getCellState(row, col);
        if (state == CellState.GIVEN) {
            int v = gameState.getBoard().getValue(row, col);
            tf.setText(String.valueOf(v));
            tf.setEditable(false);
            tf.getStyleClass().add("cell-given");
        } else {
            tf.setEditable(true);
            tf.getStyleClass().add("cell-user");
            installListener(tf, row, col);
        }

        return tf;
    }

    private void installListener(TextField tf, int row, int col) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                gameState.enterValue(row, col, 0);
                return;
            }
            // Keep only last character and only if digit 1-9
            char ch = newVal.charAt(newVal.length() - 1);
            if (ch >= '1' && ch <= '9') {
                String single = String.valueOf(ch);
                if (!tf.getText().equals(single)) {
                    tf.setText(single); // triggers listener again — guard: same value
                } else {
                    gameState.enterValue(row, col, Character.getNumericValue(ch));
                }
            } else {
                tf.setText(oldVal != null ? oldVal : "");
            }
        });

        tf.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                applyStyle(tf, "cell-selected");
            } else {
                reapplyConflictStyle(row, col);
            }
        });

        tf.setOnKeyPressed(e -> {
            int dr = 0, dc = 0;
            if      (e.getCode() == KeyCode.UP)    dr = -1;
            else if (e.getCode() == KeyCode.DOWN)  dr =  1;
            else if (e.getCode() == KeyCode.LEFT)  dc = -1;
            else if (e.getCode() == KeyCode.RIGHT) dc =  1;
            else return;
            e.consume();
            int nr = row + dr, nc = col + dc;
            if (nr >= 0 && nr < 9 && nc >= 0 && nc < 9) {
                cells[nr][nc].requestFocus();
            }
        });
    }

    @Override
    public void onGameEvent(GameEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case CELL_CHANGED, CONFLICT_DETECTED, CONFLICT_RESOLVED -> refreshConflicts();
                case HINT_APPLIED -> {
                    int r = event.getRow(), c = event.getCol();
                    if (r >= 0) {
                        if (lastHintRow >= 0) reapplyConflictStyle(lastHintRow, lastHintCol);
                        updateCellText(r, c);
                        applyStyle(cells[r][c], "cell-hint");
                        lastHintRow = r;
                        lastHintCol = c;
                    }
                    refreshConflicts();
                }
                default -> {}
            }
        });
    }

    private void refreshConflicts() {
        Set<String> conflicts = validator.findConflicts(gameState.getBoard());
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                updateCellText(r, c);
                reapplyConflictStyle(r, c, conflicts.contains(r + "," + c));
            }
        }
    }

    private void updateCellText(int row, int col) {
        if (gameState.getBoard().isGiven(row, col)) return;
        int v = gameState.getBoard().getValue(row, col);
        String text = v == 0 ? "" : String.valueOf(v);
        TextField tf = cells[row][col];
        if (!tf.getText().equals(text)) {
            // Temporarily remove listener to avoid re-entrancy
            tf.textProperty().set(text);
        }
    }

    private void reapplyConflictStyle(int row, int col) {
        Set<String> conflicts = validator.findConflicts(gameState.getBoard());
        reapplyConflictStyle(row, col, conflicts.contains(row + "," + col));
    }

    private void reapplyConflictStyle(int row, int col, boolean isConflicting) {
        if (gameState.getBoard().isGiven(row, col)) return;
        if (row == lastHintRow && col == lastHintCol) return; // keep hint highlight
        if (isConflicting) {
            applyStyle(cells[row][col], "cell-conflict");
        } else {
            applyStyle(cells[row][col], "cell-user");
        }
    }

    /** Replaces all user-state style classes with the given one. */
    private void applyStyle(TextField tf, String styleName) {
        tf.getStyleClass().removeAll("cell-user", "cell-conflict", "cell-selected", "cell-hint");
        if (!tf.getStyleClass().contains(styleName)) {
            tf.getStyleClass().add(styleName);
        }
    }

    public void resetHint() {
        if (lastHintRow >= 0) {
            reapplyConflictStyle(lastHintRow, lastHintCol);
            lastHintRow = -1;
            lastHintCol = -1;
        }
    }
}
