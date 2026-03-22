package io.bino.swing;

import io.bino.core.logic.SudokuGenerator;
import io.bino.core.model.Difficulty;
import io.bino.core.model.GameState;
import io.bino.core.model.SudokuBoard;
import io.bino.core.sample.SamplePuzzles;
import io.bino.swing.board.BoardPanel;
import io.bino.swing.control.ControlPanel;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    private GameState gameState;
    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private final SudokuGenerator generator = new SudokuGenerator();

    public MainFrame() {
        super("Sudoku");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        startNewGame(Difficulty.EASY);

        setSize(560, 680);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void startNewGame(Difficulty difficulty) {
        SudokuBoard puzzle = generator.generate(difficulty);
        initGame(puzzle, difficulty);
    }

    public void startSampleGame() {
        initGame(SamplePuzzles.easy1(), Difficulty.EASY);
    }

    private void initGame(SudokuBoard puzzle, Difficulty difficulty) {
        if (gameState != null) {
            gameState.pauseTimer();
        }

        gameState = new GameState(puzzle, difficulty);

        if (boardPanel != null) {
            getContentPane().remove(boardPanel);
        }
        if (controlPanel != null) {
            getContentPane().remove(controlPanel);
        }

        boardPanel = new BoardPanel(gameState);
        controlPanel = new ControlPanel(gameState, this);

        gameState.addGameEventListener(boardPanel);
        gameState.addGameEventListener(controlPanel);
        gameState.addGameEventListener(controlPanel.getTimerLabel());

        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        gameState.startTimer();
        revalidate();
        repaint();
    }

    public void showCompletion() {
        long seconds = gameState.getElapsedSeconds();
        int mistakes = gameState.getMistakeCount();
        String time = String.format("%d:%02d", seconds / 60, seconds % 60);
        JOptionPane.showMessageDialog(this,
                "Puzzle solved!\nTime: " + time + "  |  Mistakes: " + mistakes,
                "Congratulations",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
