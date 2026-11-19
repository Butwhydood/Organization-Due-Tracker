package model;

import exceptions.InvalidInputException;

/**
 * An ordinary member of the organization. Regular members pay the full
 * {@link Member#BASE_DUES}.
 */
public class RegularMember extends Member {

    /**
     * Creates a regular member.
     *
     * @param studentId the unique student ID
     * @param fullName  the full name
     * @param course    the course
     * @param yearLevel the year level from 1 to 6
     * @throws InvalidInputException if any value breaks a rule
     */
    public RegularMember(String studentId, String fullName, String course, int yearLevel) {
        super(studentId, fullName, course, yearLevel);
    }

    /**
     * Returns the full dues amount.
     *
     * @return {@link Member#BASE_DUES}
     */
    @Override
    public double getDuesAmount() {
        return BASE_DUES;
    }

    /**
     * Returns the type label.
     *
     * @return "Regular"
     */
    @Override
    public String getMemberType() {
        return "Regular";
    }

    /**
     * Converts this member to a CSV line.
     *
     * @return {@code REGULAR,studentId,fullName,course,yearLevel}
     */
    @Override
    public String toCsv() {
        return String.join(",", "REGULAR", getStudentId(), getFullName(), getCourse(),
                String.valueOf(getYearLevel()));
    }
}
