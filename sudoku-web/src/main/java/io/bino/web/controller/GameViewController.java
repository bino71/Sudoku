package io.bino.web.controller;

import io.bino.web.dto.BoardDto;
import io.bino.web.session.GameSession;
import io.bino.core.model.Difficulty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class GameViewController {

    private final GameSession gameSession;

    public GameViewController(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    @GetMapping("/thymeleaf")
    public String index(Model model) {
        try {
            model.addAttribute("board", BoardDto.from(gameSession.getOrFail()));
        } catch (ResponseStatusException e) {
            gameSession.newGame(Difficulty.EASY);
            model.addAttribute("board", BoardDto.from(gameSession.getOrFail()));
        }
        return "game";
    }
}
