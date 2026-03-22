package io.bino.swing.control;

import io.bino.core.event.GameEvent;
import io.bino.core.event.GameEventListener;
import io.bino.core.model.GameState;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * JLabel that displays elapsed game time, updated via TIMER_TICK events.
 */
public class TimerLabel extends JLabel implements GameEventListener {

    private GameState gameState;

    public TimerLabel(GameState gameState) {
        super("0:00");
        this.gameState = gameState;
    }

    public void rebind(GameState newState) {
        this.gameState = newState;
        setText("0:00");
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event.getType() == GameEvent.Type.TIMER_TICK) {
            SwingUtilities.invokeLater(this::updateDisplay);
        }
    }

    private void updateDisplay() {
        long seconds = gameState.getElapsedSeconds();
        setText(String.format("%d:%02d", seconds / 60, seconds % 60));
    }
}
