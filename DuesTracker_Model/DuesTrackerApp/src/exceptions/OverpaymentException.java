package exceptions;

/**
 * Thrown when a payment is larger than what the member (or fine) still owes.
 * <p>
 * It is a <b>checked</b> exception, so every caller of
 * {@link model.Payable#pay(double)} must handle the rule explicitly.
 */
public class OverpaymentException extends Exception {

    private static final long serialVersionUID = 1L;

    private final double attemptedAmount;
    private final double remainingBalance;

    /**
     * Creates the exception.
     *
     * @param attemptedAmount  the amount the user tried to pay
     * @param remainingBalance the amount that is actually still owed
     */
    public OverpaymentException(double attemptedAmount, double remainingBalance) {
        super(String.format("Payment of PHP %.2f is more than the remaining balance of PHP %.2f.",
                attemptedAmount, remainingBalance));
        this.attemptedAmount = attemptedAmount;
        this.remainingBalance = remainingBalance;
    }

    /**
     * Returns the amount that was rejected.
     *
     * @return the attempted payment amount
     */
    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    /**
     * Returns the balance that was still owed when the payment was rejected.
     *
     * @return the remaining balance
     */
    public double getRemainingBalance() {
        return remainingBalance;
    }
}
