package horst.haering.de.swing;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SudokuSwingApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default look and feel
        }
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
