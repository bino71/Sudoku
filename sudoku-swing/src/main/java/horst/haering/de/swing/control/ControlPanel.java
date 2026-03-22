package horst.haering.de.swing.control;

import horst.haering.de.core.event.GameEvent;
import horst.haering.de.core.event.GameEventListener;
import horst.haering.de.core.logic.SudokuSolver;
import horst.haering.de.core.model.Difficulty;
import horst.haering.de.core.model.GameState;
import horst.haering.de.core.model.SudokuBoard;
import horst.haering.de.swing.MainFrame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Control bar below the board with New Game, Hint, Solve, Reset buttons,
 * difficulty chooser, timer, and mistake counter.
 */
public class ControlPanel extends JPanel implements GameEventListener {

    private final MainFrame mainFrame;
    private GameState gameState;
    private final JLabel mistakeLabel = new JLabel("Mistakes: 0");
    private final TimerLabel timerLabel;
    private final JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());

    private static final SudokuSolver solver = new SudokuSolver();

    public ControlPanel(GameState gameState, MainFrame mainFrame) {
        this.gameState = gameState;
        this.mainFrame = mainFrame;
        this.timerLabel = new TimerLabel(gameState);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 12, 12, 12));

        // Row 1: buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton newGameBtn = new JButton("New Game");
        JButton hintBtn    = new JButton("Hint");
        JButton solveBtn   = new JButton("Solve");
        JButton resetBtn   = new JButton("Reset");

        newGameBtn.addActionListener(e -> {
            Difficulty d = (Difficulty) difficultyBox.getSelectedItem();
            mainFrame.startNewGame(d);
        });
        hintBtn.addActionListener(e -> gameState.applyHint());
        solveBtn.addActionListener(e -> {
            SudokuBoard copy = gameState.getBoard().copy();
            if (solver.solve(copy)) {
                int[][] arr = copy.toArray();
                for (int r = 0; r < 9; r++) {
                    for (int c = 0; c < 9; c++) {
                        if (!gameState.getBoard().isGiven(r, c)) {
                            int existing = gameState.getBoard().getValue(r, c);
                            if (existing != arr[r][c]) {
                                gameState.enterValue(r, c, arr[r][c]);
                            }
                        }
                    }
                }
            }
        });
        resetBtn.addActionListener(e -> {
            gameState.getBoard().clearUserCells();
            gameState.resetTimer();
            gameState.startTimer();
            // Force a full UI refresh by firing a synthetic cell-changed event
            gameState.fireEvent(new GameEvent(GameEvent.Type.CELL_CHANGED));
        });

        buttonRow.add(newGameBtn);
        buttonRow.add(difficultyBox);
        buttonRow.add(hintBtn);
        buttonRow.add(solveBtn);
        buttonRow.add(resetBtn);

        // Row 2: status
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        timerLabel.setForeground(new Color(0, 100, 0));
        mistakeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusRow.add(new JLabel("Time:"));
        statusRow.add(timerLabel);
        statusRow.add(mistakeLabel);

        add(Box.createVerticalStrut(4));
        add(buttonRow);
        add(Box.createVerticalStrut(4));
        add(statusRow);
    }

    public TimerLabel getTimerLabel() {
        return timerLabel;
    }

    /** Called when MainFrame creates a new game — rewire to new GameState. */
    public void rebind(GameState newState) {
        this.gameState = newState;
        this.timerLabel.rebind(newState);
        mistakeLabel.setText("Mistakes: 0");
    }

    @Override
    public void onGameEvent(GameEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()) {
                case MISTAKE_INCREMENTED ->
                    mistakeLabel.setText("Mistakes: " + gameState.getMistakeCount());
                case PUZZLE_COMPLETE ->
                    mainFrame.showCompletion();
                default -> {}
            }
        });
    }
}
