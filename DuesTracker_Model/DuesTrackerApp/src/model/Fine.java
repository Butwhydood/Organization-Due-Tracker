package model;

import exceptions.InvalidInputException;
import exceptions.OverpaymentException;
import java.util.Locale;

/**
 * A fine charged to a member, for example for missing a required meeting.
 * <p>
 * A fine is not a kind of member, but it can be paid off just like dues, so
 * it implements {@link Payable} directly.
 */
public class Fine implements Payable {

    private final String fineId;
    private final String studentId;
    private String reason;
    private final double amount;
    private double amountPaid;

    /**
     * Creates an unpaid fine.
     *
     * @param fineId    the unique fine ID, such as F-001
     * @param studentId the ID of the member who is fined
     * @param reason    why the fine was given
     * @param amount    the fine amount, greater than zero
     * @throws InvalidInputException if any value breaks a rule
     */
    public Fine(String fineId, String studentId, String reason, double amount) {
        this(fineId, studentId, reason, amount, 0.0);
    }

    /**
     * Creates a fine that may already be partly paid (used when loading from a file).
     *
     * @param fineId     the unique fine ID
     * @param studentId  the ID of the member who is fined
     * @param reason     why the fine was given
     * @param amount     the fine amount, greater than zero
     * @param amountPaid how much was already paid, from 0 up to the fine amount
     * @throws InvalidInputException if any value breaks a rule
     */
    public Fine(String fineId, String studentId, String reason, double amount, double amountPaid) {
        this.fineId = Validator.requireId("Fine ID", fineId);
        this.studentId = Validator.requireId("Student ID", studentId);
        setReason(reason);
        this.amount = Validator.requirePositiveAmount("Fine amount", amount);
        if (amountPaid < 0 || amountPaid > this.amount) {
            throw new InvalidInputException("Amount paid must be between 0 and the fine amount.");
        }
        this.amountPaid = Validator.round2(amountPaid);
    }

    /**
     * Returns the fine ID (never changes).
     *
     * @return the fine ID
     */
    public String getFineId() {
        return fineId;
    }

    /**
     * Returns the ID of the member who was fined (never changes).
     *
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Returns the reason for the fine.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Changes the reason for the fine.
     *
     * @param reason the new reason, 1 to 60 characters, no commas
     * @throws InvalidInputException if the reason breaks a rule
     */
    public final void setReason(String reason) {
        this.reason = Validator.requireText("Reason", reason, 60);
    }

    /**
     * Returns the fine amount.
     *
     * @return the amount due
     */
    @Override
    public double getAmountDue() {
        return amount;
    }

    /**
     * Returns how much of the fine has been paid.
     *
     * @return the amount paid
     */
    @Override
    public double getAmountPaid() {
        return amountPaid;
    }

    /**
     * Pays part or all of the fine.
     *
     * @param payment the amount to pay
     * @throws OverpaymentException if the amount is more than the balance
     * @throws InvalidInputException if the amount is not positive
     */
    @Override
    public void pay(double payment) throws OverpaymentException {
        double clean = Validator.requirePositiveAmount("Payment amount", payment);
        if (clean > getBalance() + 0.004) {
            throw new OverpaymentException(clean, getBalance());
        }
        amountPaid = Validator.round2(amountPaid + clean);
    }

    /**
     * Converts this fine to one CSV line.
     *
     * @return {@code fineId,studentId,reason,amount,amountPaid}
     */
    public String toCsv() {
        return String.join(",", fineId, studentId, reason,
                String.format(Locale.ROOT, "%.2f", amount),
                String.format(Locale.ROOT, "%.2f", amountPaid));
    }

    /**
     * Rebuilds a fine from a CSV line made by {@link #toCsv()}.
     *
     * @param line the CSV line
     * @return the fine
     * @throws InvalidInputException if the line is damaged
     */
    public static Fine fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new InvalidInputException("Empty fine line.");
        }
        String[] p = line.split(",", -1);
        if (p.length != 5) {
            throw new InvalidInputException("Fine line should have 5 fields but has " + p.length + ": " + line);
        }
        try {
            return new Fine(p[0], p[1], p[2], Double.parseDouble(p[3].trim()), Double.parseDouble(p[4].trim()));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Fine amount is not a number in line: " + line);
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s | %s | %s | PHP %.2f (balance PHP %.2f)",
                fineId, studentId, reason, amount, getBalance());
    }
}
