package exceptions;

/**
 * Thrown when attempting to add a record whose identifier (such as a student ID
 * or fine ID) already exists in storage.
 * <p>
 * It is a <b>checked</b> exception so callers explicitly handle duplicate ID
 * conflicts and display a friendly message rather than crashing.
 */
public class DuplicateIdException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String entityType;
    private final String duplicateId;

    /**
     * Creates a duplicate ID exception with a clear message.
     *
     * @param entityType  the kind of record (such as "Member" or "Fine")
     * @param duplicateId the ID that already exists
     */
    public DuplicateIdException(String entityType, String duplicateId) {
        super(String.format("%s with ID '%s' already exists.", entityType, duplicateId));
        this.entityType = entityType;
        this.duplicateId = duplicateId;
    }

    /**
     * Returns the type of record that caused the conflict.
     *
     * @return the entity type label
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Returns the duplicate identifier.
     *
     * @return the duplicate ID
     */
    public String getDuplicateId() {
        return duplicateId;
    }
}