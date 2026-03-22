package io.bino.core.sample;

import io.bino.core.model.Difficulty;
import io.bino.core.model.SudokuBoard;

import java.util.List;

/**
 * Built-in sample puzzles for testing and quick-start play.
 * The "field2" puzzle is salvaged from the original prototype code
 * with the duplicate-2 bug in row 9 corrected.
 */
public class SamplePuzzles {

    /**
     * Original "field2" puzzle from the prototype, corrected.
     * Row 9 originally had duplicate 2s at indices [8][5] and [8][7].
     * Fixed: [8][7] changed from 2 to 6.
     * Difficulty: EASY.
     */
    public static SudokuBoard field2Easy() {
        int[][] clues = {
            { 2, 0, 6, 8, 0, 4, 0, 1, 7 },
            { 7, 0, 4, 0, 0, 0, 6, 8, 0 },
            { 5, 1, 8, 0, 3, 0, 9, 2, 4 },
            { 0, 2, 9, 0, 0, 5, 1, 4, 6 },
            { 6, 4, 0, 9, 0, 0, 8, 7, 0 },
            { 0, 7, 0, 0, 4, 0, 0, 3, 9 },
            { 1, 0, 3, 0, 0, 0, 0, 9, 0 },
            { 4, 0, 2, 0, 0, 0, 0, 5, 0 },
            { 9, 0, 7, 0, 0, 2, 0, 6, 0 }  // [8][7] fixed: 2 → 6
        };
        return SudokuBoard.fromGivens(clues);
    }

    /** A straightforward EASY puzzle. */
    public static SudokuBoard easy1() {
        int[][] clues = {
            { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
            { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
            { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
            { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
            { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
            { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
            { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
            { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
            { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        };
        return SudokuBoard.fromGivens(clues);
    }

    /** Returns all built-in puzzles for a given difficulty. */
    public static List<SudokuBoard> forDifficulty(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> List.of(field2Easy(), easy1());
            case MEDIUM, HARD -> List.of();
        };
    }
}
