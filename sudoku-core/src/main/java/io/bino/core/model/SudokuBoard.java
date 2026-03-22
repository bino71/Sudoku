package io.bino.core.model;

import io.bino.core.event.GameEvent;
import io.bino.core.event.GameEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 9x9 Sudoku board. Tracks which cells are pre-filled givens (non-editable)
 * and which are user-entered. Observable via {@link GameEventListener}.
 */
public class SudokuBoard {

    private final int[][] grid = new int[9][9];
    private final boolean[][] given = new boolean[9][9];
    private final List<GameEventListener> listeners = new ArrayList<>();

    private SudokuBoard() {}

    /**
     * Creates a board from a 9x9 array of clue values.
     * Non-zero cells become GIVEN (immutable) cells; zero cells are empty.
     */
    public static SudokuBoard fromGivens(int[][] clues) {
        SudokuBoard board = new SudokuBoard();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                board.grid[r][c] = clues[r][c];
                board.given[r][c] = clues[r][c] != 0;
            }
        }
        return board;
    }

    /** Deep copy — used by solver/generator so they never mutate the live board. */
    public SudokuBoard copy() {
        SudokuBoard copy = new SudokuBoard();
        for (int r = 0; r < 9; r++) {
            System.arraycopy(this.grid[r], 0, copy.grid[r], 0, 9);
            System.arraycopy(this.given[r], 0, copy.given[r], 0, 9);
        }
        return copy;
    }

    public int getValue(int row, int col) {
        return grid[row][col];
    }

    public boolean isGiven(int row, int col) {
        return given[row][col];
    }

    public CellState getCellState(int row, int col) {
        if (given[row][col]) return CellState.GIVEN;
        return grid[row][col] == 0 ? CellState.USER_EMPTY : CellState.USER_FILLED;
    }

    /**
     * Sets a user cell value. Pass 0 to clear. Fires {@code CELL_CHANGED}.
     *
     * @throws IllegalArgumentException if the cell is a GIVEN cell
     * @throws IllegalArgumentException if value is not in range 0–9
     */
    public void setValue(int row, int col, int value) {
        if (given[row][col]) {
            throw new IllegalArgumentException("Cannot modify given cell [" + row + "," + col + "]");
        }
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be 0–9, got: " + value);
        }
        grid[row][col] = value;
        fireEvent(new GameEvent(GameEvent.Type.CELL_CHANGED, row, col));
    }

    /** Clears all user-entered values, restoring the board to the initial puzzle state. */
    public void clearUserCells() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!given[r][c]) {
                    grid[r][c] = 0;
                }
            }
        }
    }

    /** Returns true when all 81 cells have a non-zero value. */
    public boolean isFilled() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) return false;
            }
        }
        return true;
    }

    /** Defensive copy of the underlying grid array. */
    public int[][] toArray() {
        int[][] copy = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, 9);
        }
        return copy;
    }

    public void addGameEventListener(GameEventListener l) {
        listeners.add(l);
    }

    public void removeGameEventListener(GameEventListener l) {
        listeners.remove(l);
    }

    private void fireEvent(GameEvent event) {
        for (GameEventListener l : listeners) {
            l.onGameEvent(event);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("+-------+-------+-------+\n");
        for (int r = 0; r < 9; r++) {
            sb.append("| ");
            for (int c = 0; c < 9; c++) {
                int v = grid[r][c];
                sb.append(v == 0 ? "." : v);
                if (c == 2 || c == 5) sb.append(" | ");
                else sb.append(' ');
            }
            sb.append("|\n");
            if (r == 2 || r == 5) sb.append("+-------+-------+-------+\n");
        }
        sb.append("+-------+-------+-------+");
        return sb.toString();
    }
}
