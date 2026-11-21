package gui;

import exceptions.InvalidInputException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import storage.MemberStore;

/**
 * Main application window housing the styled header, Members, Fines, and Dues
 * Report tabs, and status bar.
 */
public class MainFrame extends JFrame {

    static final Color BG_APP = new Color(0xF8, 0xFA, 0xFC);
    static final Color BG_CARD = Color.WHITE;
    static final Color NAVY_DARK = new Color(0x0F, 0x17, 0x2A);
    static final Color ACCENT_BLUE = new Color(0x1E, 0x3A, 0x8A);
    static final Color BORDER_SUBTLE = new Color(0xCB, 0xD5, 0xE1);
    static final Color STATUS_PAID = new Color(0x0F, 0x76, 0x6E);
    static final Color STATUS_PARTIAL = new Color(0xB4, 0x53, 0x09);
    static final Color STATUS_UNPAID = new Color(0xB9, 0x1C, 0x1C);

    private final MemberStore store;
    private final MembersPanel membersPanel;
    private final FinesPanel finesPanel;
    private final ReportPanel reportPanel;
    private final JLabel statusLabel;

    /**
     * Creates the main window wired to the given {@link MemberStore}.
     *
     * @param store the backing store implementation
     */
    public MainFrame(MemberStore store) {
        super("Student Organization Dues Tracker");
        this.store = store;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1020, 640);
        setMinimumSize(new java.awt.Dimension(820, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);

        Runnable refreshAll = this::refreshAllTabs;
        this.membersPanel = new MembersPanel(store, refreshAll);
        this.finesPanel = new FinesPanel(store, refreshAll);
        this.reportPanel = new ReportPanel(store);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabs.setBackground(BG_APP);
        tabs.addTab("Members", membersPanel);
        tabs.addTab("Fines", finesPanel);
        tabs.addTab("Dues Report", reportPanel);
        tabs.addChangeListener(e -> reportPanel.refreshData());

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        this.statusLabel = new JLabel("Ready");
        this.statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        this.statusLabel.setForeground(new Color(0x33, 0x41, 0x55));

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        statusBar.setBackground(BG_CARD);
        statusBar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_SUBTLE));
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData(false);
                dispose();
                System.exit(0);
            }
        });

        refreshAllTabs();
    }

    static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setBackground(BG_CARD);
        btn.setForeground(NAVY_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return btn;
    }

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(NAVY_DARK);
        bar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel titles = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);

        JLabel title = new JLabel("Student Organization Dues Tracker");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Membership dues, fines, payment tracking, and treasurer summary");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitle.setForeground(new Color(0xCB, 0xD5, 0xE1));

        titles.add(title);
        titles.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        actions.setOpaque(false);

        JButton reloadButton = createSecondaryButton("Reload Data");
        reloadButton.addActionListener(e -> {
            var warnings = store.load();
            refreshAllTabs();
            statusLabel.setText("Reloaded data (" + store.getAll().size() + " members).");
            if (!warnings.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        String.join("\n", warnings),
                        "Skipped Lines During Reload",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        JButton saveButton = createPrimaryButton("Save Changes");
        saveButton.addActionListener(e -> saveData(true));

        actions.add(reloadButton);
        actions.add(saveButton);

        bar.add(titles, BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private void saveData(boolean showConfirmation) {
        try {
            store.save();
            statusLabel.setText("Saved changes to CSV files.");
            if (showConfirmation) {
                JOptionPane.showMessageDialog(
                        this,
                        "All members, payments, and fines were saved.",
                        "Saved",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void refreshAllTabs() {
        membersPanel.refreshTable();
        finesPanel.refreshTable();
        reportPanel.refreshData();
        statusLabel.setText("Members: " + store.getAll().size()
                + "  |  Fines: " + store.getFines().size());
    }
}