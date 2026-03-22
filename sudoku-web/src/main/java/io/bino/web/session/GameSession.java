package io.bino.web.session;

import io.bino.core.logic.SudokuGenerator;
import io.bino.core.model.Difficulty;
import io.bino.core.model.GameState;
import io.bino.core.model.SudokuBoard;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.server.ResponseStatusException;

@Component
@SessionScope
public class GameSession {

    private GameState gameState;

    public GameState getOrFail() {
        if (gameState == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active game. Call GET /api/game/new first.");
        }
        return gameState;
    }

    public void newGame(Difficulty difficulty) {
        if (gameState != null) {
            gameState.pauseTimer();
        }
        SudokuBoard board = new SudokuGenerator().generate(difficulty);
        gameState = new GameState(board, difficulty);
        // NOTE: Do NOT call gameState.startTimer() — timer is client-side in web context
    }
}
