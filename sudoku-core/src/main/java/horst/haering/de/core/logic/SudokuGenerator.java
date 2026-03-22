package horst.haering.de.core.logic;

import horst.haering.de.core.model.Difficulty;
import horst.haering.de.core.model.SudokuBoard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates Sudoku puzzles with a guaranteed unique solution.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Fill a blank board with a valid complete solution using backtracking
 *       with shuffled candidate lists (random but valid).
 *   <li>Remove cells one by one in random order, checking after each removal
 *       that the puzzle still has exactly one solution. Skip cells where
 *       removal would create ambiguity.
 * </ol>
 */
public class SudokuGenerator {

    private final Random random;
    private final SudokuSolver solver;

    public SudokuGenerator() {
        this(new Random());
    }

    public SudokuGenerator(Random random) {
        this.random = random;
        this.solver = new SudokuSolver();
    }

    /** Generates a puzzle of the given difficulty with a unique solution. */
    public SudokuBoard generate(Difficulty difficulty) {
        SudokuBoard complete = generateCompleteBoard();
        return removeCells(complete, difficulty.getTargetGivens());
    }

    /** Fills a blank board with a random valid complete solution. */
    public SudokuBoard generateCompleteBoard() {
        SudokuBoard board = SudokuBoard.fromGivens(new int[9][9]);
        fillBoard(board);
        return board;
    }

    private boolean fillBoard(SudokuBoard board) {
        // Find first empty cell
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.getValue(r, c) == 0) {
                    List<Integer> digits = shuffledDigits();
                    for (int digit : digits) {
                        if (solver.isLegal(board, r, c, digit)) {
                            board.setValue(r, c, digit);
                            if (fillBoard(board)) return true;
                            board.setValue(r, c, 0);
                        }
                    }
                    return false; // no digit worked — backtrack
                }
            }
        }
        return true; // all cells filled
    }

    /**
     * Removes cells from a complete board until {@code targetGivens} clues remain,
     * always preserving a unique solution.
     */
    public SudokuBoard removeCells(SudokuBoard complete, int targetGivens) {
        SudokuBoard board = complete.copy();

        // Build list of all 81 positions and shuffle
        List<int[]> positions = new ArrayList<>(81);
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                positions.add(new int[]{r, c});
            }
        }
        Collections.shuffle(positions, random);

        int givens = 81;
        for (int[] pos : positions) {
            if (givens <= targetGivens) break;
            int r = pos[0], c = pos[1];
            int saved = board.getValue(r, c);
            board.setValue(r, c, 0);

            // Check uniqueness — if ambiguous, restore the cell
            SudokuBoard testCopy = board.copy();
            if (solver.countSolutions(testCopy, 2) != 1) {
                board.setValue(r, c, saved);
            } else {
                givens--;
            }
        }

        // Convert to a proper GIVEN board (freeze remaining non-zero cells)
        return SudokuBoard.fromGivens(board.toArray());
    }

    private List<Integer> shuffledDigits() {
        List<Integer> digits = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Collections.shuffle(digits, random);
        return digits;
    }
}
