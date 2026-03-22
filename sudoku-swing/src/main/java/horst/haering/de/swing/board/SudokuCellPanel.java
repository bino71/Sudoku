package horst.haering.de.swing.board;

import horst.haering.de.core.model.CellState;
import horst.haering.de.core.model.GameState;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;

/**
 * Single Sudoku cell backed by a JTextField.
 * Given cells are read-only with a distinct background.
 * User cells accept only single digits 1-9.
 */
public class SudokuCellPanel extends JTextField {

    // Visual state colors
    static final Color COLOR_GIVEN      = new Color(230, 230, 230);
    static final Color COLOR_NORMAL     = Color.WHITE;
    static final Color COLOR_CONFLICT   = new Color(255, 180, 180);
    static final Color COLOR_SELECTED   = new Color(180, 210, 255);
    static final Color COLOR_HINT       = new Color(180, 255, 200);

    private final int row;
    private final int col;
    private final GameState gameState;
    private boolean isConflicting = false;
    private boolean isHinted = false;

    public SudokuCellPanel(int row, int col, GameState gameState, BiConsumer<Integer, Integer> focusNeighbor) {
        this.row = row;
        this.col = col;
        this.gameState = gameState;

        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(new LineBorder(new Color(180, 180, 180), 1));

        CellState state = gameState.getBoard().getCellState(row, col);
        boolean given = (state == CellState.GIVEN);

        if (given) {
            int v = gameState.getBoard().getValue(row, col);
            setText(String.valueOf(v));
            setFont(new Font("SansSerif", Font.BOLD, 20));
            setEditable(false);
            setBackground(COLOR_GIVEN);
            setForeground(new Color(40, 40, 40));
        } else {
            setFont(new Font("SansSerif", Font.PLAIN, 20));
            setEditable(true);
            setBackground(COLOR_NORMAL);
            setForeground(new Color(20, 20, 160));
            installFilter();
        }

        // Arrow keys navigate between cells; suppress default JTextField caret movement
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP    -> focusNeighbor.accept(row - 1, col);
                    case KeyEvent.VK_DOWN  -> focusNeighbor.accept(row + 1, col);
                    case KeyEvent.VK_LEFT  -> focusNeighbor.accept(row, col - 1);
                    case KeyEvent.VK_RIGHT -> focusNeighbor.accept(row, col + 1);
                }
            }
        });
    }

    private void installFilter() {
        ((AbstractDocument) getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length,
                                String text, AttributeSet attrs) throws BadLocationException {
                if (text == null || text.isEmpty()) {
                    // Allow deletion
                    super.replace(fb, 0, fb.getDocument().getLength(), "", attrs);
                    gameState.enterValue(row, col, 0);
                    return;
                }
                // Only accept single digit 1-9
                if (text.length() == 1 && text.charAt(0) >= '1' && text.charAt(0) <= '9') {
                    super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                    gameState.enterValue(row, col, Character.getNumericValue(text.charAt(0)));
                }
                // Reject anything else silently
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String text,
                                     AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, text, attr);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length)
                    throws BadLocationException {
                super.replace(fb, 0, fb.getDocument().getLength(), "", null);
                gameState.enterValue(row, col, 0);
            }
        });
    }

    /** Update the display value without triggering the document filter. */
    public void updateValue() {
        int v = gameState.getBoard().getValue(row, col);
        if (v == 0) {
            if (!getText().isEmpty()) setText("");
        } else {
            String s = String.valueOf(v);
            if (!s.equals(getText())) setText(s);
        }
    }

    public void setConflicting(boolean conflicting) {
        if (this.isConflicting == conflicting) return;
        this.isConflicting = conflicting;
        refreshBackground();
    }

    public void setHinted(boolean hinted) {
        this.isHinted = hinted;
        refreshBackground();
    }

    private void refreshBackground() {
        if (gameState.getBoard().isGiven(row, col)) {
            setBackground(COLOR_GIVEN);
        } else if (isConflicting) {
            setBackground(COLOR_CONFLICT);
        } else if (isHinted) {
            setBackground(COLOR_HINT);
        } else {
            setBackground(COLOR_NORMAL);
        }
    }

    public int getCellRow() { return row; }
    public int getCellCol() { return col; }
}
