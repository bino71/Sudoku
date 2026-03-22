package io.bino.web.dto;

import io.bino.core.logic.SudokuValidator;
import io.bino.core.model.GameState;
import io.bino.core.model.SudokuBoard;

import java.util.Set;

public record BoardDto(
        int[][] values,
        boolean[][] given,
        boolean[][] conflicts,
        boolean complete,
        int mistakeCount,
        String difficulty
) {
    public static BoardDto from(GameState state) {
        SudokuBoard board = state.getBoard();
        SudokuValidator validator = new SudokuValidator();
        Set<String> conflictKeys = validator.findConflicts(board);
        int[][] values = board.toArray();
        boolean[][] given = new boolean[9][9];
        boolean[][] conflicts = new boolean[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                given[r][c] = board.isGiven(r, c);
                conflicts[r][c] = conflictKeys.contains(r + "," + c);
            }
        }
        return new BoardDto(values, given, conflicts,
                state.isComplete(), state.getMistakeCount(),
                state.getDifficulty().name());
    }
}
