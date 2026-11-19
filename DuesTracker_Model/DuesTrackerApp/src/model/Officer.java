package model;

import exceptions.InvalidInputException;

/**
 * An officer of the organization, such as the President or Treasurer. Officers
 * receive a discount on their dues in return for their service.
 */
public class Officer extends Member {

    /** The share of the base dues an officer is excused from (50%). */
    public static final double OFFICER_DISCOUNT = 0.50;

    private String position;

    /**
     * Creates an officer with a position.
     *
     * @param studentId the unique student ID
     * @param fullName  the full name
     * @param course    the course
     * @param yearLevel the year level from 1 to 6
     * @param position  the officer position, such as Treasurer
     * @throws InvalidInputException if any value breaks a rule
     */
    public Officer(String studentId, String fullName, String course, int yearLevel, String position) {
        super(studentId, fullName, course, yearLevel);
        setPosition(position);
    }

    /**
     * Creates an officer whose position is not decided yet (overload). The
     * position defaults to "Officer".
     *
     * @param studentId the unique student ID
     * @param fullName  the full name
     * @param course    the course
     * @param yearLevel the year level from 1 to 6
     * @throws InvalidInputException if any value breaks a rule
     */
    public Officer(String studentId, String fullName, String course, int yearLevel) {
        this(studentId, fullName, course, yearLevel, "Officer");
    }

    /**
     * Returns the officer position.
     *
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /**
     * Changes the officer position.
     *
     * @param position the new position, 1 to 30 characters, no commas
     * @throws InvalidInputException if the position breaks a rule
     */
    public final void setPosition(String position) {
        this.position = Validator.requireText("Position", position, 30);
    }

    /**
     * Returns the discounted dues amount.
     *
     * @return the base dues reduced by {@link #OFFICER_DISCOUNT}
     */
    @Override
    public double getDuesAmount() {
        return Validator.round2(BASE_DUES * (1 - OFFICER_DISCOUNT));
    }

    /**
     * Returns the type label.
     *
     * @return "Officer"
     */
    @Override
    public String getMemberType() {
        return "Officer";
    }

    /**
     * Converts this officer to a CSV line.
     *
     * @return {@code OFFICER,studentId,fullName,course,yearLevel,position}
     */
    @Override
    public String toCsv() {
        return String.join(",", "OFFICER", getStudentId(), getFullName(), getCourse(),
                String.valueOf(getYearLevel()), position);
    }

    /**
     * Returns a one-line summary that includes the position.
     *
     * @return a text summary of this officer
     */
    @Override
    public String toString() {
        return super.toString() + " | " + position;
    }
}
