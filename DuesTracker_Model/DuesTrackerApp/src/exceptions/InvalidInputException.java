package exceptions;

/**
 * Thrown when a value breaks one of the dues tracker's input rules, such as a
 * blank name, a year level out of range, or a damaged line in a data file.
 * <p>
 * It is unchecked (extends {@link IllegalArgumentException}) so that validating
 * setters and constructors stay simple. The GUI catches it and shows
 * {@link #getMessage()} in a dialog, which is always a friendly sentence.
 */
public class InvalidInputException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a user-friendly message.
     *
     * @param message what was wrong and how to fix it
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
