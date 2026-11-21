package gui;

import exceptions.MemberNotFoundException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Member;
import model.Officer;
import storage.MemberStore;

/**
 * Screen for viewing, searching, adding, editing, deleting members, and
 * recording dues payments.
 */
public class MembersPanel extends JPanel {

    private static final String[] COLUMNS = {
        "Student ID", "Type", "Full Name", "Course", "Year",
        "Position", "Dues (PHP)", "Paid (PHP)", "Balance (PHP)", "Status"
    };

    private final MemberStore store;
    private final Runnable onDataChanged;
    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Member> displayedMembers;

    /**
     * Creates the members management panel.
     *
     * @param store         the member store
     * @param onDataChanged callback invoked after mutations so other views update
     */
    public MembersPanel(MemberStore store, Runnable onDataChanged) {
        super(new BorderLayout(10, 10));
        this.store = store;
        this.onDataChanged = onDataChanged;
        setBackground(MainFrame.BG_APP);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        // Top search card
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchPanel.setBackground(MainFrame.BG_CARD);
        searchPanel.setBorder(new javax.swing.border.CompoundBorder(
                new LineBorder(MainFrame.BORDER_SUBTLE, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        searchLabel.setForeground(MainFrame.NAVY_DARK);

        this.searchField = new JTextField(26);
        this.searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        JButton searchButton = MainFrame.createPrimaryButton("Search");
        JButton clearButton = MainFrame.createSecondaryButton("Clear");

        searchField.addActionListener(e -> refreshTable());
        searchButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        add(searchPanel, BorderLayout.NORTH);

        // Styled table
        this.tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(tableModel);
        this.table.setRowHeight(26);
        this.table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        this.table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        this.table.getTableHeader().setBackground(new Color(0xE2, 0xE8, 0xF0));
        this.table.getTableHeader().setForeground(MainFrame.NAVY_DARK);
        this.table.setGridColor(new Color(0xE2, 0xE8, 0xF0));
        this.table.setFillsViewportHeight(true);

        DefaultTableCellRenderer rightCurrencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
                    c.setForeground(MainFrame.NAVY_DARK);
                }
                return c;
            }
        };

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                String status = value == null ? "" : value.toString();
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
                    if ("PAID".equals(status)) {
                        c.setForeground(MainFrame.STATUS_PAID);
                    } else if ("PARTIAL".equals(status)) {
                        c.setForeground(MainFrame.STATUS_PARTIAL);
                    } else {
                        c.setForeground(MainFrame.STATUS_UNPAID);
                    }
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(6).setCellRenderer(rightCurrencyRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(rightCurrencyRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(rightCurrencyRenderer);
        table.getColumnModel().getColumn(9).setCellRenderer(statusRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(MainFrame.BORDER_SUBTLE, 1, true));
        add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.setBackground(MainFrame.BG_APP);

        JButton addBtn = MainFrame.createPrimaryButton("Add Member");
        JButton editBtn = MainFrame.createSecondaryButton("Edit Member");
        JButton deleteBtn = MainFrame.createSecondaryButton("Delete Member");
        JButton payBtn = MainFrame.createPrimaryButton("Record Payment");
        JButton historyBtn = MainFrame.createSecondaryButton("Payment History");

        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelectedMember());
        payBtn.addActionListener(e -> openPaymentDialog());
        historyBtn.addActionListener(e -> showPaymentHistory());

        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(deleteBtn);
        actions.add(payBtn);
        actions.add(historyBtn);
        add(actions, BorderLayout.SOUTH);

        refreshTable();
    }

    /**
     * Refreshes the table rows using the current search query.
     */
    public void refreshTable() {
        String query = searchField.getText();
        this.displayedMembers = (query == null || query.isBlank())
                ? store.getAll()
                : store.search(query);

        tableModel.setRowCount(0);
        for (Member m : displayedMembers) {
            String position = (m instanceof Officer officer)
                    ? officer.getPosition()
                    : "-";
            tableModel.addRow(new Object[]{
                m.getStudentId(),
                m.getMemberType(),
                m.getFullName(),
                m.getCourse(),
                m.getYearLevel(),
                position,
                String.format("%.2f", m.getAmountDue()),
                String.format("%.2f", m.getAmountPaid()),
                String.format("%.2f", m.getBalance()),
                m.getStatus()
            });
        }
    }

    private Member getSelectedMember() {
        int row = table.getSelectedRow();
        if (row < 0 || displayedMembers == null || row >= displayedMembers.size()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a member from the table first.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }
        return displayedMembers.get(row);
    }

    private Frame ownerFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void openAddDialog() {
        MemberDialog dialog = new MemberDialog(ownerFrame(), store, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            onDataChanged.run();
        }
    }

    private void openEditDialog() {
        Member selected = getSelectedMember();
        if (selected == null) {
            return;
        }
        MemberDialog dialog = new MemberDialog(ownerFrame(), store, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            onDataChanged.run();
        }
    }

    private void deleteSelectedMember() {
        Member selected = getSelectedMember();
        if (selected == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete member " + selected.getFullName() + " (" + selected.getStudentId() + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            try {
                store.delete(selected.getStudentId());
                onDataChanged.run();
            } catch (MemberNotFoundException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Not Found",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void openPaymentDialog() {
        Member selected = getSelectedMember();
        if (selected == null) {
            return;
        }
        PaymentDialog dialog = new PaymentDialog(ownerFrame(), selected);
        dialog.setVisible(true);
        if (dialog.isPaymentRecorded()) {
            onDataChanged.run();
        }
    }

    private void showPaymentHistory() {
        Member selected = getSelectedMember();
        if (selected == null) {
            return;
        }
        var payments = selected.getPayments();
        if (payments.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No payments recorded yet for " + selected.getFullName() + ".",
                    "Payment History",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        StringBuilder sb = new StringBuilder("Payments for ")
                .append(selected.getFullName())
                .append(" (").append(selected.getStudentId()).append("):\n\n");
        for (var p : payments) {
            sb.append(String.format("• %s | PHP %.2f | %s%n",
                    p.getDate(), p.getAmount(), p.getNote()));
        }
        JOptionPane.showMessageDialog(
                this,
                sb.toString(),
                "Payment History",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}