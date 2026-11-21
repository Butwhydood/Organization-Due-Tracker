package exceptions;

/**
 * Thrown when an operation (such as lookup, payment recording, update, or
 * deletion) references a student ID that does not exist in the store.
 * <p>
 * It is a <b>checked</b> exception so callers handle missing members cleanly.
 */
public class MemberNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String studentId;

    /**
     * Creates a member-not-found exception with a clear message.
     *
     * @param studentId the student ID that could not be found
     */
    public MemberNotFoundException(String studentId) {
        super(String.format("No member found with student ID '%s'.", studentId));
        this.studentId = studentId;
    }

    /**
     * Returns the missing student ID.
     *
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }
}