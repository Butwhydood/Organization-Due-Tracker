import gui.MainFrame;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import storage.FileMemberStore;
import storage.MemberStore;

/**
 * Entry point for the Student Organization Dues Tracker GUI application.
 */
public final class Main {

    private Main() {
        // Utility entry class
    }

    /**
     * Launches the application on the Swing Event Dispatch Thread.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MemberStore store = new FileMemberStore();
            List<String> warnings = store.load();

            MainFrame frame = new MainFrame(store);
            frame.setVisible(true);

            if (!warnings.isEmpty()) {
                StringBuilder sb = new StringBuilder(
                        "Some damaged CSV lines were skipped while loading:\n\n");
                for (String warning : warnings) {
                    sb.append("• ").append(warning).append("\n");
                }
                JOptionPane.showMessageDialog(
                        frame,
                        sb.toString(),
                        "Data Load Warnings",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
    }
}