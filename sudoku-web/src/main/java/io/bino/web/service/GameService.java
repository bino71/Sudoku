package io.bino.web.service;

import io.bino.core.logic.SudokuSolver;
import io.bino.core.model.GameState;
import io.bino.core.model.SudokuBoard;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final SudokuSolver solver = new SudokuSolver();

    public void applyFullSolution(GameState state) {
        SudokuBoard copy = state.getBoard().copy();
        copy.clearUserCells();
        if (!solver.solve(copy)) return;
        int[][] solution = copy.toArray();
        SudokuBoard live = state.getBoard();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!live.isGiven(r, c) && live.getValue(r, c) != solution[r][c]) {
                    state.enterValue(r, c, solution[r][c]);
                }
            }
        }
    }
}
