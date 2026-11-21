package gui;

import exceptions.InvalidInputException;
import exceptions.OverpaymentException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Member;
import model.Validator;

/**
 * Modal dialog for recording a dues payment against a {@link Member}.
 */
public class PaymentDialog extends JDialog {

    private final Member member;
    private final JTextField amountField;
    private final JTextField noteField;
    private boolean paymentRecorded;

    /**
     * Creates a payment dialog for the specified member.
     *
     * @param owner  parent frame
     * @param member member receiving the payment
     */
    public PaymentDialog(Frame owner, Member member) {
        super(owner, "Record Dues Payment", true);
        this.member = member;

        setSize(420, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(MainFrame.BG_APP);

        JPanel infoAndForm = new JPanel(new GridLayout(4, 2, 10, 10));
        infoAndForm.setBackground(MainFrame.BG_CARD);
        infoAndForm.setBorder(new EmptyBorder(16, 18, 12, 18));

        Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

        this.amountField = new JTextField();
        this.amountField.setFont(plainFont);

        this.noteField = new JTextField("Dues payment");
        this.noteField.setFont(plainFont);

        JLabel memberVal = new JLabel(member.getFullName() + " (" + member.getStudentId() + ")");
        memberVal.setFont(plainFont);

        JLabel balanceVal = new JLabel(String.format("PHP %.2f", member.getBalance()));
        balanceVal.setFont(boldFont);
        balanceVal.setForeground(member.getBalance() > 0 ? MainFrame.STATUS_UNPAID : MainFrame.STATUS_PAID);

        infoAndForm.add(label("Member:", boldFont));
        infoAndForm.add(memberVal);
        infoAndForm.add(label("Current Balance:", boldFont));
        infoAndForm.add(balanceVal);
        infoAndForm.add(label("Amount (PHP):", boldFont));
        infoAndForm.add(amountField);
        infoAndForm.add(label("Note:", boldFont));
        infoAndForm.add(noteField);

        add(infoAndForm, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBackground(MainFrame.BG_APP);
        JButton payBtn = MainFrame.createPrimaryButton("Record Payment");
        JButton cancelBtn = MainFrame.createSecondaryButton("Cancel");

        payBtn.addActionListener(e -> onRecordPayment());
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(payBtn);
        buttons.add(cancelBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Returns whether a payment was recorded.
     *
     * @return {@code true} if recorded
     */
    public boolean isPaymentRecorded() {
        return paymentRecorded;
    }

    private JLabel label(String text, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(MainFrame.NAVY_DARK);
        return lbl;
    }

    private void onRecordPayment() {
        try {
            double amount = Validator.parseAmount("Payment amount", amountField.getText());
            member.pay(amount, noteField.getText());
            paymentRecorded = true;
            dispose();
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