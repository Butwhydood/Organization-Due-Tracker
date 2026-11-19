package model;

import exceptions.InvalidInputException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * One payment made by a member. Payments are immutable: once recorded they
 * cannot change, which keeps the payment history trustworthy.
 */
public final class Payment {

    private final String studentId;
    private final double amount;
    private final LocalDate date;
    private final String note;

    /**
     * Creates a payment after validating every value.
     *
     * @param studentId the ID of the member who paid
     * @param amount    the amount paid, greater than zero
     * @param date      the date of payment, not in the future
     * @param note      a short note (blank becomes "Dues payment")
     * @throws InvalidInputException if any value breaks a rule
     */
    public Payment(String studentId, double amount, LocalDate date, String note) {
        this.studentId = Validator.requireId("Student ID", studentId);
        this.amount = Validator.requirePositiveAmount("Payment amount", amount);
        if (date == null) {
            throw new InvalidInputException("Payment date cannot be empty.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidInputException("Payment date cannot be in the future.");
        }
        this.date = date;
        this.note = Validator.optionalText("Note", note, 60, "Dues payment");
    }

    /**
     * Returns the ID of the member who paid.
     *
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Returns the amount paid.
     *
     * @return the amount in pesos
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Returns the payment date.
     *
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the payment note.
     *
     * @return the note text
     */
    public String getNote() {
        return note;
    }

    /**
     * Converts this payment to one CSV line: {@code studentId,amount,date,note}.
     *
     * @return the CSV line
     */
    public String toCsv() {
        return String.join(",", studentId, String.format(Locale.ROOT, "%.2f", amount), date.toString(), note);
    }

    /**
     * Rebuilds a payment from a CSV line made by {@link #toCsv()}.
     *
     * @param line the CSV line
     * @return the payment
     * @throws InvalidInputException if the line is damaged
     */
    public static Payment fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new InvalidInputException("Empty payment line.");
        }
        String[] parts = line.split(",", -1);
        if (parts.length != 4) {
            throw new InvalidInputException("Payment line should have 4 fields but has " + parts.length + ": " + line);
        }
        try {
            return new Payment(parts[0], Double.parseDouble(parts[1].trim()),
                    LocalDate.parse(parts[2].trim()), parts[3]);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Payment amount is not a number in line: " + line);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Payment date is not valid (use yyyy-mm-dd) in line: " + line);
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s  PHP %.2f  (%s)", date, amount, note);
    }
}
