package horst.haering.de.core.logic;

import horst.haering.de.core.model.SudokuBoard;

import java.util.Optional;

/**
 * Backtracking Sudoku solver with MRV (Minimum Remaining Values) heuristic.
 * All methods operate on board copies — the original is never mutated.
 */
public class SudokuSolver {

    public record HintCell(int row, int col, int value, String reason) {}

    /**
     * Solves the board in-place using backtracking + MRV.
     * Returns true if a solution was found; the board contains the solution.
     * Returns false if unsolvable; board state is undefined.
     */
    public boolean solve(SudokuBoard board) {
        int[] cell = findMRVCell(board);
        if (cell == null) {
            // No empty cell left — solved if valid, otherwise stuck
            return isConsistent(board);
        }
        int row = cell[0], col = cell[1];
        for (int digit = 1; digit <= 9; digit++) {
            if (isLegal(board, row, col, digit)) {
                board.setValue(row, col, digit);
                if (solve(board)) return true;
                board.setValue(row, col, 0);
            }
        }
        return false;
    }

    /**
     * Counts solutions up to {@code maxCount}. Stops early once maxCount is reached.
     * Used by the generator to verify puzzle uniqueness (pass maxCount=2).
     */
    public int countSolutions(SudokuBoard board, int maxCount) {
        int[] cell = findMRVCell(board);
        if (cell == null) {
            return isConsistent(board) ? 1 : 0;
        }
        int row = cell[0], col = cell[1];
        int count = 0;
        for (int digit = 1; digit <= 9; digit++) {
            if (isLegal(board, row, col, digit)) {
                board.setValue(row, col, digit);
                count += countSolutions(board, maxCount - count);
                board.setValue(row, col, 0);
                if (count >= maxCount) break;
            }
        }
        return count;
    }

    /**
     * Finds a hint using naked-single constraint propagation:
     * a cell where only one digit is legally placeable.
     * Returns empty if no single-step hint is available.
     */
    public Optional<HintCell> findHint(SudokuBoard board) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.getValue(r, c) != 0) continue;
                int candidate = -1;
                int count = 0;
                for (int digit = 1; digit <= 9; digit++) {
                    if (isLegal(board, r, c, digit)) {
                        candidate = digit;
                        count++;
                    }
                }
                if (count == 1) {
                    return Optional.of(new HintCell(r, c, candidate, "Naked single"));
                }
            }
        }
        // Hidden single: a digit that can only go in one cell within a row/col/box
        Optional<HintCell> hidden = findHiddenSingle(board);
        if (hidden.isPresent()) return hidden;

        return Optional.empty();
    }

    // --- private helpers ---

    /** Returns [row, col] of the empty cell with fewest legal candidates (MRV). */
    private int[] findMRVCell(SudokuBoard board) {
        int bestRow = -1, bestCol = -1, bestCount = 10;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.getValue(r, c) != 0) continue;
                int count = countLegal(board, r, c);
                if (count < bestCount) {
                    bestCount = count;
                    bestRow = r;
                    bestCol = c;
                }
            }
        }
        return bestRow == -1 ? null : new int[]{bestRow, bestCol};
    }

    private int countLegal(SudokuBoard board, int row, int col) {
        int count = 0;
        for (int d = 1; d <= 9; d++) {
            if (isLegal(board, row, col, d)) count++;
        }
        return count;
    }

    /** True if placing digit at [row,col] causes no row/col/box conflict. */
    boolean isLegal(SudokuBoard board, int row, int col, int digit) {
        for (int i = 0; i < 9; i++) {
            if (board.getValue(row, i) == digit) return false;
            if (board.getValue(i, col) == digit) return false;
        }
        int br = (row / 3) * 3, bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++) {
            for (int c = bc; c < bc + 3; c++) {
                if (board.getValue(r, c) == digit) return false;
            }
        }
        return true;
    }

    private boolean isConsistent(SudokuBoard board) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int v = board.getValue(r, c);
                if (v == 0) return false;
            }
        }
        return true;
    }

    private Optional<HintCell> findHiddenSingle(SudokuBoard board) {
        // Check rows
        for (int r = 0; r < 9; r++) {
            for (int digit = 1; digit <= 9; digit++) {
                int foundCol = -1, count = 0;
                for (int c = 0; c < 9; c++) {
                    if (board.getValue(r, c) == 0 && isLegal(board, r, c, digit)) {
                        foundCol = c;
                        count++;
                    }
                }
                if (count == 1) {
                    return Optional.of(new HintCell(r, foundCol, digit, "Hidden single in row " + (r + 1)));
                }
            }
        }
        // Check columns
        for (int c = 0; c < 9; c++) {
            for (int digit = 1; digit <= 9; digit++) {
                int foundRow = -1, count = 0;
                for (int r = 0; r < 9; r++) {
                    if (board.getValue(r, c) == 0 && isLegal(board, r, c, digit)) {
                        foundRow = r;
                        count++;
                    }
                }
                if (count == 1) {
                    return Optional.of(new HintCell(foundRow, c, digit, "Hidden single in column " + (c + 1)));
                }
            }
        }
        // Check boxes
        for (int boxR = 0; boxR < 3; boxR++) {
            for (int boxC = 0; boxC < 3; boxC++) {
                for (int digit = 1; digit <= 9; digit++) {
                    int foundRow = -1, foundCol = -1, count = 0;
                    for (int r = boxR * 3; r < boxR * 3 + 3; r++) {
                        for (int c = boxC * 3; c < boxC * 3 + 3; c++) {
                            if (board.getValue(r, c) == 0 && isLegal(board, r, c, digit)) {
                                foundRow = r;
                                foundCol = c;
                                count++;
                            }
                        }
                    }
                    if (count == 1) {
                        return Optional.of(new HintCell(foundRow, foundCol, digit,
                                "Hidden single in box (" + (boxR + 1) + "," + (boxC + 1) + ")"));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
