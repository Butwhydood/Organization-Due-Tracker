package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Totals for the treasurer. Every method works on the {@link Payable}
 * interface, so the same code totals members, fines, or a mix of both.
 */
public final class DuesReport {

    private DuesReport() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Adds up everything that is owed.
     *
     * @param items the payable items
     * @return the total amount due
     */
    public static double totalDue(List<? extends Payable> items) {
        double total = 0;
        for (Payable item : items) {
            total += item.getAmountDue();
        }
        return Validator.round2(total);
    }

    /**
     * Adds up everything that has been collected.
     *
     * @param items the payable items
     * @return the total amount paid
     */
    public static double totalCollected(List<? extends Payable> items) {
        double total = 0;
        for (Payable item : items) {
            total += item.getAmountPaid();
        }
        return Validator.round2(total);
    }

    /**
     * Adds up everything that is still unpaid.
     *
     * @param items the payable items
     * @return the total remaining balance
     */
    public static double totalOutstanding(List<? extends Payable> items) {
        double total = 0;
        for (Payable item : items) {
            total += item.getBalance();
        }
        return Validator.round2(total);
    }

    /**
     * Collects the items that still owe something.
     *
     * @param <T>   the kind of payable item
     * @param items the payable items
     * @return a new list of the items with a balance above zero
     */
    public static <T extends Payable> List<T> unpaid(List<T> items) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (!item.isFullyPaid()) {
                result.add(item);
            }
        }
        return result;
    }
}
