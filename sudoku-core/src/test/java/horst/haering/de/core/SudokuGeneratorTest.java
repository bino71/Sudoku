package horst.haering.de.core;

import horst.haering.de.core.logic.SudokuGenerator;
import horst.haering.de.core.logic.SudokuSolver;
import horst.haering.de.core.logic.SudokuValidator;
import horst.haering.de.core.model.Difficulty;
import horst.haering.de.core.model.SudokuBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Generator tests use a fixed seed and only test EASY difficulty to keep runtime under 5s.
 * The underlying algorithm is the same for all difficulties — only targetGivens differs.
 */
class SudokuGeneratorTest {

    private static final long SEED = 42L;
    private SudokuGenerator generator;
    private SudokuValidator validator;
    private SudokuSolver solver;

    @BeforeEach
    void setUp() {
        generator = new SudokuGenerator(new Random(SEED));
        validator = new SudokuValidator();
        solver = new SudokuSolver();
    }

    @Test
    void generateCompleteBoardIsValid() {
        SudokuBoard board = generator.generateCompleteBoard();
        assertTrue(validator.isValid(board),
                "Complete board should be fully filled and conflict-free:\n" + board);
    }

    @Test
    void generateEasyPuzzleHasUniqueSolution() {
        SudokuBoard puzzle = generator.generate(Difficulty.EASY);
        SudokuBoard copy = puzzle.copy();
        assertEquals(1, solver.countSolutions(copy, 2),
                "Generated EASY puzzle must have exactly one solution");
    }

    @Test
    void generateEasyPuzzleSolvesToValidBoard() {
        SudokuBoard puzzle = generator.generate(Difficulty.EASY);
        SudokuBoard copy = puzzle.copy();
        assertTrue(solver.solve(copy));
        assertTrue(validator.isValid(copy));
    }

    @Test
    void generateEasyGivenCellCountInRange() {
        SudokuBoard puzzle = generator.generate(Difficulty.EASY);
        int givens = countGivens(puzzle);
        int target = Difficulty.EASY.getTargetGivens();
        assertTrue(givens >= target - 2 && givens <= target + 10,
                "Expected ~" + target + " givens for EASY, got " + givens);
    }

    @Test
    void deterministicWithSameSeed() {
        SudokuGenerator g1 = new SudokuGenerator(new Random(123));
        SudokuGenerator g2 = new SudokuGenerator(new Random(123));
        SudokuBoard b1 = g1.generate(Difficulty.EASY);
        SudokuBoard b2 = g2.generate(Difficulty.EASY);
        int[][] a1 = b1.toArray(), a2 = b2.toArray();
        for (int r = 0; r < 9; r++) {
            assertArrayEquals(a1[r], a2[r], "Row " + r + " should match for same seed");
        }
    }

    @Test
    void removeCellsPreservesGivenCells() {
        SudokuBoard complete = generator.generateCompleteBoard();
        SudokuBoard puzzle = generator.removeCells(complete, 35);
        // All given cells must have values from the complete board
        int[][] orig = complete.toArray();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (puzzle.isGiven(r, c)) {
                    assertEquals(orig[r][c], puzzle.getValue(r, c),
                            "Given cell [" + r + "," + c + "] must match original");
                }
            }
        }
    }

    private int countGivens(SudokuBoard board) {
        int count = 0;
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (board.isGiven(r, c)) count++;
        return count;
    }
}
