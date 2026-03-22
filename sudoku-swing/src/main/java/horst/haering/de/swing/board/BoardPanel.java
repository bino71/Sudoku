package horst.haering.de.swing.board;

import horst.haering.de.core.event.GameEvent;
import horst.haering.de.core.event.GameEventListener;
import horst.haering.de.core.logic.SudokuValidator;
import horst.haering.de.core.model.GameState;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Set;

/**
 * 9x9 grid of {@link SudokuCellPanel}s.
 * Custom paintComponent draws thick 3px borders around each 3x3 box.
 */
public class BoardPanel extends JPanel implements GameEventListener {

    private static final int CELL_SIZE = 56;
    private static final int BOARD_SIZE = CELL_SIZE * 9;

    private final SudokuCellPanel[][] cells = new SudokuCellPanel[9][9];
    private final GameState gameState;
    private final SudokuValidator validator = new SudokuValidator();

    private int lastHintRow = -1, lastHintCol = -1;

    public BoardPanel(GameState gameState) {
        this.gameState = gameState;

        setLayout(new GridLayout(9, 9));
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                SudokuCellPanel cell = new SudokuCellPanel(r, c, gameState, this::focusCell);
                cells[r][c] = cell;
                add(cell);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Thin cell borders are handled by each cell's LineBorder.
        // Draw thick box borders at box boundaries.
        g2.setColor(new Color(60, 60, 60));
        g2.setStroke(new java.awt.BasicStroke(3f));

        // Outer border
        g2.drawRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        // Inner box lines at columns 3 and 6
        for (int box = 1; box < 3; box++) {
            int x = box * CELL_SIZE * 3;
            g2.drawLine(x, 0, x, BOARD_SIZE);
        }
        // Inner box lines at rows 3 and 6
        for (int box = 1; box < 3; box++) {
            int y = box * CELL_SIZE * 3;
            g2.drawLine(0, y, BOARD_SIZE, y);
        }
    }

    @Override
    public void onGameEvent(GameEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()) {
                case CELL_CHANGED, CONFLICT_DETECTED, CONFLICT_RESOLVED -> refreshConflicts();
                case HINT_APPLIED -> {
                    int r = event.getRow(), c = event.getCol();
                    if (r >= 0) {
                        if (lastHintRow >= 0) cells[lastHintRow][lastHintCol].setHinted(false);
                        cells[r][c].updateValue();
                        cells[r][c].setHinted(true);
                        lastHintRow = r;
                        lastHintCol = c;
                    }
                    refreshConflicts();
                }
                case PUZZLE_COMPLETE -> {
                    refreshConflicts();
                    // MainFrame listens separately for the dialog
                }
                default -> { /* TIMER_TICK, MISTAKE_INCREMENTED handled by ControlPanel */ }
            }
        });
    }

    private void refreshConflicts() {
        Set<String> conflicts = validator.findConflicts(gameState.getBoard());
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                boolean conflicting = conflicts.contains(r + "," + c);
                cells[r][c].setConflicting(conflicting);
                cells[r][c].updateValue();
            }
        }
        repaint();
    }

    public void focusCell(int row, int col) {
        if (row < 0 || row > 8 || col < 0 || col > 8) return;
        cells[row][col].requestFocusInWindow();
    }

    public void resetHint() {
        if (lastHintRow >= 0) {
            cells[lastHintRow][lastHintCol].setHinted(false);
            lastHintRow = -1;
            lastHintCol = -1;
        }
    }
}
