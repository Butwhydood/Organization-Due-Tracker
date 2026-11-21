package gui;

import exceptions.DuplicateIdException;
import exceptions.InvalidInputException;
import exceptions.MemberNotFoundException;
import exceptions.OverpaymentException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Fine;
import model.Validator;
import storage.MemberStore;

/**
 * Screen for viewing organization fines, adding new fines, and paying fines.
 */
public class FinesPanel extends JPanel {

    private static final String[] COLUMNS = {
        "Fine ID", "Student ID", "Reason", "Amount Due (PHP)",
        "Paid (PHP)", "Balance (PHP)", "Status"
    };

    private final MemberStore store;
    private final Runnable onDataChanged;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Fine> currentFines;

    /**
     * Creates the fines management panel.
     *
     * @param store         member store
     * @param onDataChanged callback invoked after mutations
     */
    public FinesPanel(MemberStore store, Runnable onDataChanged) {
        super(new BorderLayout(10, 10));
        this.store = store;
        this.onDataChanged = onDataChanged;
        setBackground(MainFrame.BG_APP);
        setBorder(new EmptyBorder(12, 14, 12, 14));

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

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
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
                    c.setForeground("PAID".equals(status) ? MainFrame.STATUS_PAID : MainFrame.STATUS_UNPAID);
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(MainFrame.BORDER_SUBTLE, 1, true));
        add(scrollPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.setBackground(MainFrame.BG_APP);

        JButton addFineBtn = MainFrame.createPrimaryButton("Add Fine");
        JButton payFineBtn = MainFrame.createSecondaryButton("Pay Selected Fine");

        addFineBtn.addActionListener(e -> openAddFineDialog());
        payFineBtn.addActionListener(e -> openPayFineDialog());

        actions.add(addFineBtn);
        actions.add(payFineBtn);
        add(actions, BorderLayout.SOUTH);

        refreshTable();
    }

    /**
     * Refreshes the fines table from the store.
     */
    public void refreshTable() {
        this.currentFines = store.getFines();
        tableModel.setRowCount(0);
        for (Fine f : currentFines) {
            tableModel.addRow(new Object[]{
                f.getFineId(),
                f.getStudentId(),
                f.getReason(),
                String.format("%.2f", f.getAmountDue()),
                String.format("%.2f", f.getAmountPaid()),
                String.format("%.2f", f.getBalance()),
                f.isFullyPaid() ? "PAID" : "UNPAID"
            });
        }
    }

    private void openAddFineDialog() {
        JTextField fineIdField = new JTextField();
        JTextField studentIdField = new JTextField();
        JTextField reasonField = new JTextField();
        JTextField amountField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Fine ID:"));
        panel.add(fineIdField);
        panel.add(new JLabel("Student ID:"));
        panel.add(studentIdField);
        panel.add(new JLabel("Reason:"));
        panel.add(reasonField);
        panel.add(new JLabel("Amount (PHP):"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Fine",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Validator.parseAmount("Fine amount", amountField.getText());
                Fine fine = new Fine(
                        fineIdField.getText(),
                        studentIdField.getText(),
                        reasonField.getText(),
                        amount
                );
                store.addFine(fine);
                onDataChanged.run();
            } catch (InvalidInputException | DuplicateIdException | MemberNotFoundException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Fine Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void openPayFineDialog() {
        int row = table.getSelectedRow();
        if (row < 0 || currentFines == null || row >= currentFines.size()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a fine first.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        Fine selected = currentFines.get(row);
        String input = JOptionPane.showInputDialog(
                this,
                String.format("Fine %s balance: PHP %.2f%nEnter payment amount:",
                        selected.getFineId(), selected.getBalance()),
                "Pay Fine",
                JOptionPane.PLAIN_MESSAGE
        );
        if (input != null) {
            try {
                double amount = Validator.parseAmount("Fine payment", input);
                selected.pay(amount);
                onDataChanged.run();
            } catch (InvalidInputException | OverpaymentException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Payment Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}