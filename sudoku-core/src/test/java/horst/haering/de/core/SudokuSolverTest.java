package horst.haering.de.core;

import horst.haering.de.core.logic.SudokuSolver;
import horst.haering.de.core.logic.SudokuValidator;
import horst.haering.de.core.model.SudokuBoard;
import horst.haering.de.core.sample.SamplePuzzles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SudokuSolverTest {

    private SudokuSolver solver;
    private SudokuValidator validator;

    @BeforeEach
    void setUp() {
        solver = new SudokuSolver();
        validator = new SudokuValidator();
    }

    @Test
    void solvesField2Puzzle() {
        SudokuBoard board = SamplePuzzles.field2Easy().copy();
        boolean solved = solver.solve(board);
        assertTrue(solved, "Solver should find a solution");
        assertTrue(validator.isValid(board), "Solution must be valid:\n" + board);
    }

    @Test
    void solvesEasy1Puzzle() {
        SudokuBoard board = SamplePuzzles.easy1().copy();
        assertTrue(solver.solve(board));
        assertTrue(validator.isValid(board));
    }

    @Test
    void detectsUnsolvableBoard() {
        // easy1 complete solution with ALL 81 cells given except [0][2],
        // but with [0][0] changed from 5→4. That makes [0][2] need a value absent
        // from row 0={4,3,_,6,7,8,9,1,2}, col 2={4,2,8,9,6,3,1,7,5}, box0={4,3,6,7,2,1,9,8}.
        // Row 0 needs 5; col 2 already has 5; box 0 needs 5. Col 2 blocks 5, so 0 candidates.
        // MRV picks [0][2] immediately; no digit is legal → solver returns false fast.
        int[][] clues = {
            { 4, 3, 0, 6, 7, 8, 9, 1, 2 },
            { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
            { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
            { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
            { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
            { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
            { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
            { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
            { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
        };
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        assertFalse(solver.solve(board));
    }

    @Test
    void countSolutionsUniquePuzzle() {
        // Near-complete board (only 2 cells empty) must have exactly 1 solution fast.
        // easy1 complete solution with [0][2]=4 and [0][6]=9 removed: these two cells
        // are in different boxes (0 and 2). Box 0 forces [0][2]=4, box 2 forces [0][6]=9.
        int[][] clues = {
            { 5, 3, 0, 6, 7, 8, 0, 1, 2 },   // [0][2] and [0][6] empty
            { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
            { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
            { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
            { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
            { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
            { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
            { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
            { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
        };
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        assertEquals(1, solver.countSolutions(board, 2),
                "Near-complete board with 2 forced cells must have exactly one solution");
    }

    @Test
    void countSolutionsAmbiguous() {
        // easy1 puzzle with [2][2] (=8) removed creates exactly 2 solutions.
        // Without that clue box 0 and the affected row/col leave two valid assignments,
        // so countSolutions(board, 2) must return 2 (stops as soon as 2 are found).
        int[][] clues = {
            { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
            { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
            { 0, 9, 0, 0, 0, 0, 0, 6, 0 },  // [2][2] removed (was 8)
            { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
            { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
            { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
            { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
            { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
            { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        };
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        assertEquals(2, solver.countSolutions(board, 2),
                "Puzzle missing one critical clue should have exactly 2 solutions");
    }

    @Test
    void findHintNakedSingle() {
        // Build a board where row 0 has 1-8, leaving only 9 for [0][8]
        int[][] clues = new int[9][9];
        for (int c = 0; c < 8; c++) clues[0][c] = c + 1;
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        // [0][8] is the only empty cell in row 0 and must be 9
        Optional<SudokuSolver.HintCell> hint = solver.findHint(board);
        assertTrue(hint.isPresent(), "Should find a naked single hint");
        assertEquals(9, hint.get().value());
    }

    @Test
    void findHintReturnsEmptyWhenNoneAvailable() {
        // Completely empty board — no naked or hidden singles
        SudokuBoard board = SudokuBoard.fromGivens(new int[9][9]);
        Optional<SudokuSolver.HintCell> hint = solver.findHint(board);
        assertTrue(hint.isEmpty(), "Empty board should yield no constraint-based hint");
    }
}
