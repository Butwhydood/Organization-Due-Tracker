package model;

import exceptions.InvalidInputException;
import exceptions.OverpaymentException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A member of the student organization. This is the abstract parent of
 * {@link RegularMember} and {@link Officer}.
 * <p>
 * It is abstract because "a member" is never created on its own: every member
 * is either regular or an officer, and each kind has a different dues amount.
 * It implements {@link Payable} so that every member can be asked what they owe.
 */
public abstract class Member implements Payable {

    /** The standard dues per semester for a regular member, in pesos. */
    public static final double BASE_DUES = 200.00;

    private final String studentId;
    private String fullName;
    private String course;
    private int yearLevel;
    private final List<Payment> payments = new ArrayList<>();

    /**
     * Creates a member after validating every value.
     *
     * @param studentId the unique student ID (never changes after creation)
     * @param fullName  the member's full name
     * @param course    the course, such as BSCS
     * @param yearLevel the year level from 1 to 6
     * @throws InvalidInputException if any value breaks a rule
     */
    protected Member(String studentId, String fullName, String course, int yearLevel) {
        this.studentId = Validator.requireId("Student ID", studentId);
        setFullName(fullName);
        setCourse(course);
        setYearLevel(yearLevel);
    }

    // ----- abstract behavior: each subclass must decide these -----

    /**
     * Returns the dues this kind of member must pay for the semester.
     *
     * @return the dues amount, in pesos
     */
    public abstract double getDuesAmount();

    /**
     * Returns a short label for this kind of member, such as "Regular".
     *
     * @return the member type label
     */
    public abstract String getMemberType();

    /**
     * Converts this member to one CSV line for the members file. The payments
     * are stored separately (see {@link Payment#toCsv()}).
     *
     * @return the CSV line
     */
    public abstract String toCsv();

    // ----- Payable -----

    /**
     * Returns the total dues this member must pay.
     *
     * @return the same value as {@link #getDuesAmount()}
     */
    @Override
    public double getAmountDue() {
        return getDuesAmount();
    }

    /**
     * Returns the sum of all payments made by this member.
     *
     * @return the amount paid, in pesos
     */
    @Override
    public double getAmountPaid() {
        double total = 0;
        for (Payment payment : payments) {
            total += payment.getAmount();
        }
        return Validator.round2(total);
    }

    /**
     * Records a dues payment dated today.
     *
     * @param amount the amount paid
     * @throws OverpaymentException if the amount is more than the balance
     * @throws InvalidInputException if the amount is not positive
     */
    @Override
    public void pay(double amount) throws OverpaymentException {
        pay(amount, "Dues payment");
    }

    /**
     * Records a payment dated today with a note (overload).
     *
     * @param amount the amount paid
     * @param note   a short note, or blank for the default
     * @throws OverpaymentException if the amount is more than the balance
     * @throws InvalidInputException if a value breaks a rule
     */
    public void pay(double amount, String note) throws OverpaymentException {
        pay(amount, note, LocalDate.now());
    }

    /**
     * Records a payment made on a given date (overload).
     *
     * @param amount the amount paid
     * @param note   a short note, or blank for the default
     * @param date   the date of payment, not in the future
     * @throws OverpaymentException if the amount is more than the balance
     * @throws InvalidInputException if a value breaks a rule
     */
    public void pay(double amount, String note, LocalDate date) throws OverpaymentException {
        double clean = Validator.requirePositiveAmount("Payment amount", amount);
        if (clean > getBalance() + 0.004) {
            throw new OverpaymentException(clean, getBalance());
        }
        payments.add(new Payment(studentId, clean, date, note));
    }

    /**
     * Adds a payment that was loaded from a file. Unlike
     * {@link #pay(double, String, LocalDate)}, it does not check the balance,
     * so a saved history is never lost if the dues amount changes later (for
     * example when a member becomes an officer).
     *
     * @param payment the payment to restore
     * @throws InvalidInputException if the payment belongs to a different member
     */
    public void restorePayment(Payment payment) {
        if (payment == null || !payment.getStudentId().equals(studentId)) {
            throw new InvalidInputException("Payment does not belong to member " + studentId + ".");
        }
        payments.add(payment);
    }

    // ----- other behavior -----

    /**
     * Returns the payment status as text.
     *
     * @return "PAID", "PARTIAL", or "UNPAID"
     */
    public String getStatus() {
        if (isFullyPaid()) {
            return "PAID";
        }
        return getAmountPaid() > 0 ? "PARTIAL" : "UNPAID";
    }

    /**
     * Returns the member's payment history as a read-only list.
     *
     * @return the payments, oldest first
     */
    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    // ----- getters and validating setters -----

    /**
     * Returns the student ID (never changes).
     *
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Returns the full name.
     *
     * @return the name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Changes the full name.
     *
     * @param fullName the new name, 1 to 50 characters, no commas
     * @throws InvalidInputException if the name breaks a rule
     */
    public final void setFullName(String fullName) {
        this.fullName = Validator.requireText("Name", fullName, 50);
    }

    /**
     * Returns the course.
     *
     * @return the course
     */
    public String getCourse() {
        return course;
    }

    /**
     * Changes the course.
     *
     * @param course the new course, 1 to 30 characters, no commas
     * @throws InvalidInputException if the course breaks a rule
     */
    public final void setCourse(String course) {
        this.course = Validator.requireText("Course", course, 30);
    }

    /**
     * Returns the year level.
     *
     * @return the year level, 1 to 6
     */
    public int getYearLevel() {
        return yearLevel;
    }

    /**
     * Changes the year level.
     *
     * @param yearLevel a number from 1 to 6
     * @throws InvalidInputException if the number is out of range
     */
    public final void setYearLevel(int yearLevel) {
        this.yearLevel = Validator.requireRange("Year level", yearLevel, 1, 6);
    }

    /**
     * Rebuilds the right kind of member from one CSV line made by
     * {@link #toCsv()}. This is the single place that decides which subclass a
     * stored line becomes.
     *
     * @param line a line such as {@code REGULAR,2024-0001,Ana Cruz,BSCS,2}
     * @return a {@link RegularMember} or {@link Officer}
     * @throws InvalidInputException if the line is damaged
     */
    public static Member fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new InvalidInputException("Empty member line.");
        }
        String[] p = line.split(",", -1);
        try {
            switch (p[0].trim().toUpperCase()) {
                case "REGULAR":
                    requireFieldCount(p, 5, line);
                    return new RegularMember(p[1], p[2], p[3], Integer.parseInt(p[4].trim()));
                case "OFFICER":
                    requireFieldCount(p, 6, line);
                    return new Officer(p[1], p[2], p[3], Integer.parseInt(p[4].trim()), p[5]);
                default:
                    throw new InvalidInputException("Unknown member type '" + p[0] + "' in line: " + line);
            }
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Year level is not a number in line: " + line);
        }
    }

    private static void requireFieldCount(String[] parts, int expected, String line) {
        if (parts.length != expected) {
            throw new InvalidInputException("Expected " + expected + " fields but found "
                    + parts.length + " in line: " + line);
        }
    }

    /**
     * Returns a one-line summary for lists and logs.
     *
     * @return a text summary of this member
     */
    @Override
    public String toString() {
        return String.format("%s | %-9s | %-20s | %s Y%d | %s", studentId, getMemberType(),
                fullName, course, yearLevel, getStatus());
    }
}
