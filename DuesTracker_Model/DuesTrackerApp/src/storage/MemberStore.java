package storage;

import exceptions.DuplicateIdException;
import exceptions.InvalidInputException;
import exceptions.MemberNotFoundException;
import java.util.List;
import model.Fine;
import model.Member;

/**
 * Contract for managing and persisting organization members, their payments,
 * and fines. GUI classes should depend on this interface rather than concrete
 * file I/O classes.
 */
public interface MemberStore {

    /**
     * Adds a new member to the store.
     *
     * @param member the member to add
     * @throws DuplicateIdException if a member with the same student ID exists
     * @throws InvalidInputException if {@code member} is null
     */
    void add(Member member) throws DuplicateIdException;

    /**
     * Finds a member by their student ID.
     *
     * @param studentId the student ID to look up
     * @return the matching member
     * @throws MemberNotFoundException if no member has that ID
     */
    Member findById(String studentId) throws MemberNotFoundException;

    /**
     * Updates an existing member record (matched by student ID).
     *
     * @param member the updated member instance
     * @throws MemberNotFoundException if no member with that ID exists
     * @throws InvalidInputException if {@code member} is null
     */
    void update(Member member) throws MemberNotFoundException;

    /**
     * Deletes a member by their student ID.
     *
     * @param studentId the student ID to remove
     * @throws MemberNotFoundException if no member with that ID exists
     */
    void delete(String studentId) throws MemberNotFoundException;

    /**
     * Searches members by matching query text against student ID, full name,
     * course, or member type (case-insensitive).
     *
     * @param query the search text
     * @return matching members (empty list if blank or no match)
     */
    List<Member> search(String query);

    /**
     * Returns all members currently in the store.
     *
     * @return a list of members
     */
    List<Member> getAll();

    /**
     * Returns all fines currently in the store.
     *
     * @return a list of fines
     */
    List<Fine> getFines();

    /**
     * Adds a fine to the store.
     *
     * @param fine the fine to add
     * @throws DuplicateIdException if a fine with the same ID already exists
     * @throws MemberNotFoundException if the fine's student ID does not exist
     * @throws InvalidInputException if {@code fine} is null
     */
    void addFine(Fine fine) throws DuplicateIdException, MemberNotFoundException;

    /**
     * Loads members, payments, and fines from backing storage. Missing files
     * on first run are handled without crashing, and damaged lines are skipped
     * and returned as human-readable warnings.
     *
     * @return list of warning messages describing any skipped/damaged lines
     */
    List<String> load();

    /**
     * Saves all members, payments, and fines to backing storage.
     *
     * @throws InvalidInputException if writing to storage fails
     */
    void save();
}