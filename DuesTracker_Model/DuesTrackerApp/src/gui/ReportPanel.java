package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import model.DuesReport;
import model.Member;
import model.Payable;
import storage.MemberStore;

/**
 * Screen displaying treasurer summary cards and unpaid items using {@link DuesReport}.
 */
public class ReportPanel extends JPanel {

    private final MemberStore store;
    private final JLabel duesDueLabel;
    private final JLabel duesCollectedLabel;
    private final JLabel duesOutstandingLabel;

    private final JLabel finesDueLabel;
    private final JLabel finesCollectedLabel;
    private final JLabel finesOutstandingLabel;

    private final JLabel combinedDueLabel;
    private final JLabel combinedCollectedLabel;
    private final JLabel combinedOutstandingLabel;

    private final JTextArea unpaidArea;

    /**
     * Creates the treasurer report panel.
     *
     * @param store member store
     */
    public ReportPanel(MemberStore store) {
        super(new BorderLayout(12, 12));
        this.store = store;
        setBackground(MainFrame.BG_APP);
        setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 12, 12));
        cardsGrid.setBackground(MainFrame.BG_APP);

        this.duesDueLabel = metricLabel(MainFrame.NAVY_DARK);
        this.duesCollectedLabel = metricLabel(MainFrame.STATUS_PAID);
        this.duesOutstandingLabel = metricLabel(MainFrame.STATUS_UNPAID);

        this.finesDueLabel = metricLabel(MainFrame.NAVY_DARK);
        this.finesCollectedLabel = metricLabel(MainFrame.STATUS_PAID);
        this.finesOutstandingLabel = metricLabel(MainFrame.STATUS_UNPAID);

        this.combinedDueLabel = metricLabel(MainFrame.NAVY_DARK);
        this.combinedCollectedLabel = metricLabel(MainFrame.STATUS_PAID);
        this.combinedOutstandingLabel = metricLabel(MainFrame.STATUS_UNPAID);

        cardsGrid.add(buildCard("Member Dues", duesDueLabel, duesCollectedLabel, duesOutstandingLabel));
        cardsGrid.add(buildCard("Fines", finesDueLabel, finesCollectedLabel, finesOutstandingLabel));
        cardsGrid.add(buildCard("Combined Total", combinedDueLabel, combinedCollectedLabel, combinedOutstandingLabel));

        add(cardsGrid, BorderLayout.NORTH);

        JPanel unpaidCard = new JPanel(new BorderLayout(0, 8));
        unpaidCard.setBackground(MainFrame.BG_CARD);
        unpaidCard.setBorder(new CompoundBorder(
                new LineBorder(MainFrame.BORDER_SUBTLE, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel unpaidTitle = new JLabel("Unpaid Balances Summary");
        unpaidTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        unpaidTitle.setForeground(MainFrame.NAVY_DARK);
        unpaidCard.add(unpaidTitle, BorderLayout.NORTH);

        this.unpaidArea = new JTextArea();
        this.unpaidArea.setEditable(false);
        this.unpaidArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        this.unpaidArea.setBackground(MainFrame.BG_APP);
        this.unpaidArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scrollPane = new JScrollPane(unpaidArea);
        scrollPane.setBorder(new LineBorder(new Color(0xE2, 0xE8, 0xF0), 1, true));
        unpaidCard.add(scrollPane, BorderLayout.CENTER);

        add(unpaidCard, BorderLayout.CENTER);

        refreshData();
    }

    /**
     * Recomputes and displays totals and unpaid balances from the store.
     */
    public void refreshData() {
        List<Member> members = store.getAll();
        var fines = store.getFines();
        List<Payable> allPayables = new ArrayList<>();
        allPayables.addAll(members);
        allPayables.addAll(fines);

        duesDueLabel.setText(String.format("Due: PHP %.2f", DuesReport.totalDue(members)));
        duesCollectedLabel.setText(String.format("Collected: PHP %.2f", DuesReport.totalCollected(members)));
        duesOutstandingLabel.setText(String.format("Outstanding: PHP %.2f", DuesReport.totalOutstanding(members)));

        finesDueLabel.setText(String.format("Due: PHP %.2f", DuesReport.totalDue(fines)));
        finesCollectedLabel.setText(String.format("Collected: PHP %.2f", DuesReport.totalCollected(fines)));
        finesOutstandingLabel.setText(String.format("Outstanding: PHP %.2f", DuesReport.totalOutstanding(fines)));

        combinedDueLabel.setText(String.format("Due: PHP %.2f", DuesReport.totalDue(allPayables)));
        combinedCollectedLabel.setText(String.format("Collected: PHP %.2f", DuesReport.totalCollected(allPayables)));
        combinedOutstandingLabel.setText(String.format("Outstanding: PHP %.2f", DuesReport.totalOutstanding(allPayables)));

        StringBuilder sb = new StringBuilder("UNPAID MEMBERS\n--------------------------------------------------\n");
        var unpaidMembers = DuesReport.unpaid(members);
        if (unpaidMembers.isEmpty()) {
            sb.append("  All members are fully paid.\n");
        } else {
            for (Member m : unpaidMembers) {
                sb.append(String.format("  %-12s %-22s PHP %7.2f remaining%n",
                        m.getStudentId(), m.getFullName(), m.getBalance()));
            }
        }

        sb.append("\nUNPAID FINES\n--------------------------------------------------\n");
        var unpaidFines = DuesReport.unpaid(fines);
        if (unpaidFines.isEmpty()) {
            sb.append("  All fines are fully paid.\n");
        } else {
            for (var f : unpaidFines) {
                sb.append(String.format("  %-8s %-12s %-20s PHP %7.2f remaining%n",
                        f.getFineId(), f.getStudentId(), f.getReason(), f.getBalance()));
            }
        }

        unpaidArea.setText(sb.toString());
        unpaidArea.setCaretPosition(0);
    }

    private JLabel metricLabel(Color color) {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel buildCard(String title, JLabel due, JLabel collected, JLabel outstanding) {
        JPanel card = new JPanel(new GridLayout(4, 1, 4, 4));
        card.setBackground(MainFrame.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(MainFrame.BORDER_SUBTLE, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        titleLbl.setForeground(MainFrame.ACCENT_BLUE);

        card.add(titleLbl);
        card.add(due);
        card.add(collected);
        card.add(outstanding);
        return card;
    }
}