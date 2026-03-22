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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class MainFrame extends JFrame {

    private GameState gameState;
    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private final SudokuGenerator generator = new SudokuGenerator();

    public MainFrame() {
        super("Sudoku");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setIconImage(createAppIcon(32));

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

    /** Renders the app icon programmatically — a 3×3 Sudoku grid with a highlighted centre cell. */
    private static BufferedImage createAppIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g.setColor(new Color(0x1a2744));
        g.fillRoundRect(0, 0, size, size, size / 8, size / 8);

        int pad = size / 16;
        int cellGap = size / 16;
        int totalCells = 3;
        int cellSize = (size - 2 * pad - 2 * cellGap) / totalCells;

        int[][] digits = {{5, 3, 7}, {6, 9, 2}, {1, 8, 4}};
        Color dimCell   = new Color(0x26, 0x3a, 0x66, 180);
        Color highlight = new Color(0x4a90d9);
        Color dimText   = new Color(0x7a9fd4);
        Color centreText = Color.WHITE;

        Font font = new Font("Arial", Font.BOLD, Math.max(6, cellSize * 7 / 10));
        g.setFont(font);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = pad + col * (cellSize + cellGap);
                int y = pad + row * (cellSize + cellGap);
                boolean centre = row == 1 && col == 1;
                g.setColor(centre ? highlight : dimCell);
                g.fillRoundRect(x, y, cellSize, cellSize, 2, 2);
                g.setColor(centre ? centreText : dimText);
                String d = String.valueOf(digits[row][col]);
                var fm = g.getFontMetrics();
                int tx = x + (cellSize - fm.stringWidth(d)) / 2;
                int ty = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(d, tx, ty);
            }
        }
        g.dispose();
        return img;
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
