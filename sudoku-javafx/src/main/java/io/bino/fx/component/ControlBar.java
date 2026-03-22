package io.bino.fx.component;

import io.bino.core.event.GameEvent;
import io.bino.core.event.GameEventListener;
import io.bino.core.model.Difficulty;
import io.bino.core.model.GameState;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Control bar with New Game (+ difficulty selector), Hint, Solve, Reset buttons,
 * timer label, and mistake counter.
 */
public class ControlBar extends VBox implements GameEventListener {

    private final Label timerLabel  = new Label("0:00");
    private final Label mistakeLabel = new Label("Mistakes: 0");
    private GameState gameState;

    public ControlBar(GameState gameState,
                      Consumer<Difficulty> onNewGame,
                      Runnable onHint,
                      Runnable onSolve,
                      Runnable onReset) {
        this.gameState = gameState;
        setSpacing(8);
        setAlignment(Pos.CENTER);
        getStyleClass().add("control-bar");

        ComboBox<Difficulty> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll(Difficulty.values());
        diffCombo.setValue(Difficulty.EASY);
        diffCombo.getStyleClass().add("difficulty-combo");

        Button newGameBtn = button("New Game", () -> onNewGame.accept(diffCombo.getValue()));
        Button hintBtn    = button("Hint",     onHint);
        Button solveBtn   = button("Solve",    onSolve);
        Button resetBtn   = button("Reset",    onReset);

        HBox buttonRow = new HBox(8, newGameBtn, diffCombo, hintBtn, solveBtn, resetBtn);
        buttonRow.setAlignment(Pos.CENTER);

        timerLabel.getStyleClass().add("timer-label");
        mistakeLabel.getStyleClass().add("mistake-label");

        HBox statusRow = new HBox(20, new Label("Time:"), timerLabel, mistakeLabel);
        statusRow.setAlignment(Pos.CENTER);

        getChildren().addAll(buttonRow, statusRow);
    }

    public void rebind(GameState newState) {
        this.gameState = newState;
        Platform.runLater(() -> {
            timerLabel.setText("0:00");
            mistakeLabel.setText("Mistakes: 0");
        });
    }

    @Override
    public void onGameEvent(GameEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case TIMER_TICK -> {
                    long s = gameState.getElapsedSeconds();
                    timerLabel.setText(String.format("%d:%02d", s / 60, s % 60));
                }
                case MISTAKE_INCREMENTED ->
                    mistakeLabel.setText("Mistakes: " + gameState.getMistakeCount());
                default -> {}
            }
        });
    }

    private static Button button(String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("game-button");
        btn.setOnAction(e -> action.run());
        return btn;
    }
}
