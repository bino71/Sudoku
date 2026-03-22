package io.bino.web.controller;

import io.bino.core.logic.SudokuSolver;
import io.bino.core.model.Difficulty;
import io.bino.web.dto.BoardDto;
import io.bino.web.dto.EnterRequest;
import io.bino.web.dto.HintResponse;
import io.bino.web.service.GameService;
import io.bino.web.session.GameSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/game")
public class GameRestController {

    private final GameSession gameSession;
    private final GameService gameService;

    public GameRestController(GameSession gameSession, GameService gameService) {
        this.gameSession = gameSession;
        this.gameService = gameService;
    }

    @GetMapping("/new")
    public BoardDto newGame(@RequestParam(defaultValue = "EASY") Difficulty difficulty) {
        gameSession.newGame(difficulty);
        return BoardDto.from(gameSession.getOrFail());
    }

    @GetMapping("/state")
    public ResponseEntity<BoardDto> state() {
        try {
            return ResponseEntity.ok(BoardDto.from(gameSession.getOrFail()));
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/enter")
    public BoardDto enter(@RequestBody EnterRequest req) {
        gameSession.getOrFail().enterValue(req.row(), req.col(), req.value());
        return BoardDto.from(gameSession.getOrFail());
    }

    @PostMapping("/hint")
    public ResponseEntity<?> hint() {
        var state = gameSession.getOrFail();
        Optional<SudokuSolver.HintCell> hint = new SudokuSolver().findHint(state.getBoard().copy());
        if (hint.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No hint available"));
        }
        SudokuSolver.HintCell h = hint.get();
        state.enterValue(h.row(), h.col(), h.value());
        return ResponseEntity.ok(new HintResponse(h.row(), h.col(), h.value(), h.reason(),
                BoardDto.from(state)));
    }

    @PostMapping("/solve")
    public BoardDto solve() {
        var state = gameSession.getOrFail();
        gameService.applyFullSolution(state);
        return BoardDto.from(state);
    }

    @PostMapping("/reset")
    public BoardDto reset() {
        var state = gameSession.getOrFail();
        state.reset();
        return BoardDto.from(state);
    }
}
