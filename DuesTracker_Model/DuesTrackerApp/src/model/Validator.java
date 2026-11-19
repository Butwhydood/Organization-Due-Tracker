package model;

import exceptions.InvalidInputException;

/**
 * Shared validation and parsing rules, so no validation code is copied between
 * classes. Every method throws {@link InvalidInputException} with a friendly
 * message. The GUI can also call the {@code parse...} methods to convert text
 * box contents safely.
 */
public final class Validator {

    private Validator() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Checks a required text value: not null, not blank, within the length
     * limit, and free of commas and line breaks (which would break the CSV
     * files).
     *
     * @param field  the field label used in the error message
     * @param value  the text to check
     * @param maxLen the longest allowed length after trimming
     * @return the trimmed text
     * @throws InvalidInputException if the text breaks any rule
     */
    public static String requireText(String field, String value, int maxLen) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(field + " cannot be empty.");
        }
        String clean = value.trim();
        if (clean.length() > maxLen) {
            throw new InvalidInputException(field + " must be at most " + maxLen + " characters.");
        }
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\r")) {
            throw new InvalidInputException(field + " cannot contain commas or line breaks.");
        }
        return clean;
    }

    /**
     * Same as {@link #requireText(String, String, int)}, but a blank value is
     * replaced by a default instead of being rejected.
     *
     * @param field        the field label used in the error message
     * @param value        the text to check (may be null or blank)
     * @param maxLen       the longest allowed length after trimming
     * @param defaultValue the value to use when the text is blank
     * @return the trimmed text, or the default
     * @throws InvalidInputException if a non-blank text breaks a rule
     */
    public static String optionalText(String field, String value, int maxLen, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return requireText(field, value, maxLen);
    }

    /**
     * Checks an ID: 3 to 15 letters, digits, or hyphens.
     *
     * @param field the field label used in the error message
     * @param value the ID to check
     * @return the trimmed ID
     * @throws InvalidInputException if the ID is blank or has invalid characters
     */
    public static String requireId(String field, String value) {
        String clean = requireText(field, value, 15);
        if (!clean.matches("[A-Za-z0-9-]{3,15}")) {
            throw new InvalidInputException(field + " must be 3 to 15 letters, digits, or hyphens (example: 2024-0001).");
        }
        return clean;
    }

    /**
     * Checks that a whole number is inside a range.
     *
     * @param field the field label used in the error message
     * @param value the number to check
     * @param min   the smallest allowed value
     * @param max   the largest allowed value
     * @return the same value
     * @throws InvalidInputException if the value is outside the range
     */
    public static int requireRange(String field, int value, int min, int max) {
        if (value < min || value > max) {
            throw new InvalidInputException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    /**
     * Checks that a money amount is positive and realistic, and rounds it to
     * centavos.
     *
     * @param field  the field label used in the error message
     * @param amount the amount to check
     * @return the amount rounded to 2 decimal places
     * @throws InvalidInputException if the amount is not a positive, finite number up to 1,000,000
     */
    public static double requirePositiveAmount(String field, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new InvalidInputException(field + " must be greater than zero.");
        }
        if (amount > 1_000_000) {
            throw new InvalidInputException(field + " is too large (maximum is 1,000,000).");
        }
        return round2(amount);
    }

    /**
     * Converts text from a text box into a whole number.
     *
     * @param field the field label used in the error message
     * @param text  the text typed by the user
     * @return the number
     * @throws InvalidInputException if the text is empty or not a whole number
     */
    public static int parseInt(String field, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(field + " cannot be empty.");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(field + " must be a whole number (no letters or symbols).");
        }
    }

    /**
     * Converts text from a text box into a money amount.
     *
     * @param field the field label used in the error message
     * @param text  the text typed by the user
     * @return the amount rounded to 2 decimal places
     * @throws InvalidInputException if the text is empty, not a number, or not positive
     */
    public static double parseAmount(String field, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(field + " cannot be empty.");
        }
        try {
            return requirePositiveAmount(field, Double.parseDouble(text.trim()));
        } catch (NumberFormatException e) {
            throw new InvalidInputException(field + " must be a number such as 150 or 150.50.");
        }
    }

    /**
     * Rounds a money value to 2 decimal places.
     *
     * @param value the value to round
     * @return the rounded value
     */
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
