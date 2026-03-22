package horst.haering.de.core.logic;

import horst.haering.de.core.model.SudokuBoard;

import java.util.HashSet;
import java.util.Set;

/**
 * Stateless Sudoku rule validator. Detects structural conflicts
 * (duplicates in rows, columns, and 3x3 boxes). Does NOT compare
 * values against a pre-computed solution.
 */
public class SudokuValidator {

    /**
     * Returns coordinates [row, col] of every cell involved in a conflict.
     * A cell is conflicting if its value duplicates another non-zero value
     * in the same row, column, or 3x3 box.
     */
    public Set<String> findConflicts(SudokuBoard board) {
        Set<String> conflicts = new HashSet<>();

        // Check rows
        for (int r = 0; r < 9; r++) {
            int[] seen = new int[10]; // index = digit 1-9
            for (int c = 0; c < 9; c++) {
                int v = board.getValue(r, c);
                if (v != 0) seen[v]++;
            }
            for (int c = 0; c < 9; c++) {
                int v = board.getValue(r, c);
                if (v != 0 && seen[v] > 1) conflicts.add(key(r, c));
            }
        }

        // Check columns
        for (int c = 0; c < 9; c++) {
            int[] seen = new int[10];
            for (int r = 0; r < 9; r++) {
                int v = board.getValue(r, c);
                if (v != 0) seen[v]++;
            }
            for (int r = 0; r < 9; r++) {
                int v = board.getValue(r, c);
                if (v != 0 && seen[v] > 1) conflicts.add(key(r, c));
            }
        }

        // Check 3x3 boxes
        for (int boxR = 0; boxR < 3; boxR++) {
            for (int boxC = 0; boxC < 3; boxC++) {
                int[] seen = new int[10];
                for (int r = boxR * 3; r < boxR * 3 + 3; r++) {
                    for (int c = boxC * 3; c < boxC * 3 + 3; c++) {
                        int v = board.getValue(r, c);
                        if (v != 0) seen[v]++;
                    }
                }
                for (int r = boxR * 3; r < boxR * 3 + 3; r++) {
                    for (int c = boxC * 3; c < boxC * 3 + 3; c++) {
                        int v = board.getValue(r, c);
                        if (v != 0 && seen[v] > 1) conflicts.add(key(r, c));
                    }
                }
            }
        }

        return conflicts;
    }

    /** Returns true if the specific cell participates in any conflict. */
    public boolean isCellConflicting(SudokuBoard board, int row, int col) {
        return findConflicts(board).contains(key(row, col));
    }

    /**
     * Returns true when the board has no conflicts (zeros allowed).
     * Used during puzzle solving and generation.
     */
    public boolean isPartiallyValid(SudokuBoard board) {
        return findConflicts(board).isEmpty();
    }

    /**
     * Returns true when the board is completely filled and has no conflicts.
     * A fully solved board.
     */
    public boolean isValid(SudokuBoard board) {
        return board.isFilled() && findConflicts(board).isEmpty();
    }

    private static String key(int row, int col) {
        return row + "," + col;
    }
}
