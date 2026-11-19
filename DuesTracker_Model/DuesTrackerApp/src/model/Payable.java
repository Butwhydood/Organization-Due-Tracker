package model;

import exceptions.OverpaymentException;

/**
 * Anything that can be owed and paid off: a member's dues, a fine, and so on.
 * <p>
 * It is an interface because unrelated classes ({@link Member} and
 * {@link Fine}) share this ability but not their data. Storage, reports, and
 * the GUI should depend on this type where they can.
 */
public interface Payable {

    /**
     * Returns the total amount that must be paid.
     *
     * @return the amount due, in pesos
     */
    double getAmountDue();

    /**
     * Returns how much has been paid so far.
     *
     * @return the amount paid, in pesos
     */
    double getAmountPaid();

    /**
     * Records a payment.
     *
     * @param amount the amount being paid, greater than zero
     * @throws OverpaymentException if the amount is more than the remaining balance
     * @throws exceptions.InvalidInputException if the amount is not positive
     */
    void pay(double amount) throws OverpaymentException;

    /**
     * Returns how much is still owed (never negative).
     *
     * @return the remaining balance, in pesos
     */
    default double getBalance() {
        return Math.max(0.0, Validator.round2(getAmountDue() - getAmountPaid()));
    }

    /**
     * Tells whether nothing more is owed.
     *
     * @return true when the balance is zero
     */
    default boolean isFullyPaid() {
        return getBalance() < 0.005;
    }
}
