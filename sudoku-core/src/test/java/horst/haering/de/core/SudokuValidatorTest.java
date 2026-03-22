package horst.haering.de.core;

import horst.haering.de.core.logic.SudokuValidator;
import horst.haering.de.core.model.SudokuBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SudokuValidatorTest {

    private SudokuValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SudokuValidator();
    }

    @Test
    void emptyBoardHasNoConflicts() {
        SudokuBoard board = SudokuBoard.fromGivens(new int[9][9]);
        assertTrue(validator.findConflicts(board).isEmpty());
        assertTrue(validator.isPartiallyValid(board));
    }

    @Test
    void rowConflictDetected() {
        int[][] clues = new int[9][9];
        clues[3][0] = 5;
        clues[3][8] = 5; // duplicate 5 in row 3
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        Set<String> conflicts = validator.findConflicts(board);
        assertTrue(conflicts.contains("3,0"));
        assertTrue(conflicts.contains("3,8"));
    }

    @Test
    void columnConflictDetected() {
        int[][] clues = new int[9][9];
        clues[0][4] = 7;
        clues[8][4] = 7; // duplicate 7 in column 4
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        Set<String> conflicts = validator.findConflicts(board);
        assertTrue(conflicts.contains("0,4"));
        assertTrue(conflicts.contains("8,4"));
    }

    @Test
    void boxConflictDetected() {
        int[][] clues = new int[9][9];
        clues[0][0] = 3;
        clues[2][2] = 3; // duplicate 3 in top-left box
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        Set<String> conflicts = validator.findConflicts(board);
        assertTrue(conflicts.contains("0,0"));
        assertTrue(conflicts.contains("2,2"));
    }

    @Test
    void noConflictForDifferentValues() {
        int[][] clues = new int[9][9];
        for (int i = 0; i < 9; i++) clues[0][i] = i + 1; // 1-9 in row 0
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        assertTrue(validator.findConflicts(board).isEmpty());
    }

    @Test
    void isCellConflicting() {
        int[][] clues = new int[9][9];
        clues[0][0] = 9;
        clues[0][5] = 9; // duplicate 9 in row 0
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        assertTrue(validator.isCellConflicting(board, 0, 0));
        assertTrue(validator.isCellConflicting(board, 0, 5));
        assertFalse(validator.isCellConflicting(board, 1, 1));
    }

    @Test
    void isValidRequiresFilledAndNoConflicts() {
        // Valid complete solution (well-known puzzle)
        int[][] solution = {
            {5,3,4,6,7,8,9,1,2},
            {6,7,2,1,9,5,3,4,8},
            {1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},
            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},
            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
        };
        SudokuBoard board = SudokuBoard.fromGivens(solution);
        assertTrue(validator.isValid(board));
        assertTrue(validator.isPartiallyValid(board));
    }

    @Test
    void field2PuzzleIsPartiallyValid() {
        // Salvaged puzzle (after fix) should have no conflicts in the given cells
        int[][] clues = {
            { 2, 0, 6, 8, 0, 4, 0, 1, 7 },
            { 7, 0, 4, 0, 0, 0, 6, 8, 0 },
            { 5, 1, 8, 0, 3, 0, 9, 2, 4 },
            { 0, 2, 9, 0, 0, 5, 1, 4, 6 },
            { 6, 4, 0, 9, 0, 0, 8, 7, 0 },
            { 0, 7, 0, 0, 4, 0, 0, 3, 9 },
            { 1, 0, 3, 0, 0, 0, 0, 9, 0 },
            { 4, 0, 2, 0, 0, 0, 0, 5, 0 },
            { 9, 0, 7, 0, 0, 2, 0, 6, 0 }
        };
        SudokuBoard board = SudokuBoard.fromGivens(clues);
        assertTrue(validator.isPartiallyValid(board),
                "Fixed field2 puzzle should have no conflicts:\n" + board);
    }
}
