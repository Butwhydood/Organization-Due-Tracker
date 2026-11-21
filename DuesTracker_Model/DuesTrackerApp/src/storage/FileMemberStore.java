package storage;

import exceptions.DuplicateIdException;
import exceptions.InvalidInputException;
import exceptions.MemberNotFoundException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Fine;
import model.Member;
import model.Payment;

/**
 * CSV-backed implementation of {@link MemberStore} using {@link BufferedReader}
 * and {@link BufferedWriter} with try-with-resources.
 */
public class FileMemberStore implements MemberStore {

    private final Path membersFile;
    private final Path paymentsFile;
    private final Path finesFile;

    private final Map<String, Member> membersById = new LinkedHashMap<>();
    private final List<Fine> fines = new ArrayList<>();

    /**
     * Creates a store using the default {@code data/} CSV paths:
     * {@code data/members.csv}, {@code data/payments.csv}, and {@code data/fines.csv}.
     */
    public FileMemberStore() {
        this(
                Paths.get("data", "members.csv"),
                Paths.get("data", "payments.csv"),
                Paths.get("data", "fines.csv")
        );
    }

    /**
     * Creates a store with custom CSV file paths (useful for testing).
     *
     * @param membersFile  path to members CSV
     * @param paymentsFile path to payments CSV
     * @param finesFile    path to fines CSV
     */
    public FileMemberStore(Path membersFile, Path paymentsFile, Path finesFile) {
        this.membersFile = membersFile;
        this.paymentsFile = paymentsFile;
        this.finesFile = finesFile;
    }

    @Override
    public void add(Member member) throws DuplicateIdException {
        if (member == null) {
            throw new InvalidInputException("Member cannot be null.");
        }
        String id = member.getStudentId();
        if (membersById.containsKey(id)) {
            throw new DuplicateIdException("Member", id);
        }
        membersById.put(id, member);
    }

    @Override
    public Member findById(String studentId) throws MemberNotFoundException {
        Member member = membersById.get(studentId);
        if (member == null) {
            throw new MemberNotFoundException(studentId);
        }
        return member;
    }

    @Override
    public void update(Member member) throws MemberNotFoundException {
        if (member == null) {
            throw new InvalidInputException("Member cannot be null.");
        }
        String id = member.getStudentId();
        if (!membersById.containsKey(id)) {
            throw new MemberNotFoundException(id);
        }
        membersById.put(id, member);
    }

    @Override
    public void delete(String studentId) throws MemberNotFoundException {
        if (membersById.remove(studentId) == null) {
            throw new MemberNotFoundException(studentId);
        }
    }

    @Override
    public List<Member> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        String q = query.trim().toLowerCase();
        List<Member> matches = new ArrayList<>();
        for (Member m : membersById.values()) {
            if (m.getStudentId().toLowerCase().contains(q)
                    || m.getFullName().toLowerCase().contains(q)
                    || m.getCourse().toLowerCase().contains(q)
                    || m.getMemberType().toLowerCase().contains(q)) {
                matches.add(m);
            }
        }
        return matches;
    }

    @Override
    public List<Member> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(membersById.values()));
    }

    @Override
    public List<Fine> getFines() {
        return Collections.unmodifiableList(fines);
    }

    @Override
    public void addFine(Fine fine) throws DuplicateIdException, MemberNotFoundException {
        if (fine == null) {
            throw new InvalidInputException("Fine cannot be null.");
        }
        if (!membersById.containsKey(fine.getStudentId())) {
            throw new MemberNotFoundException(fine.getStudentId());
        }
        for (Fine existing : fines) {
            if (existing.getFineId().equals(fine.getFineId())) {
                throw new DuplicateIdException("Fine", fine.getFineId());
            }
        }
        fines.add(fine);
    }

    @Override
    public List<String> load() {
        membersById.clear();
        fines.clear();
        List<String> skippedWarnings = new ArrayList<>();

        loadMembers(skippedWarnings);
        loadPayments(skippedWarnings);
        loadFines(skippedWarnings);

        return skippedWarnings;
    }

    private void loadMembers(List<String> warnings) {
        if (!Files.exists(membersFile)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(membersFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Member member = Member.fromCsv(line);
                    if (membersById.containsKey(member.getStudentId())) {
                        warnings.add(String.format("%s line %d skipped: duplicate student ID '%s'.",
                                membersFile.getFileName(), lineNum, member.getStudentId()));
                        continue;
                    }
                    membersById.put(member.getStudentId(), member);
                } catch (InvalidInputException ex) {
                    warnings.add(String.format("%s line %d skipped: %s",
                            membersFile.getFileName(), lineNum, ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            warnings.add(String.format("Could not read %s: %s",
                    membersFile.getFileName(), ex.getMessage()));
        }
    }

    private void loadPayments(List<String> warnings) {
        if (!Files.exists(paymentsFile)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(paymentsFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Payment payment = Payment.fromCsv(line);
                    Member member = membersById.get(payment.getStudentId());
                    if (member == null) {
                        warnings.add(String.format("%s line %d skipped: no member found with ID '%s'.",
                                paymentsFile.getFileName(), lineNum, payment.getStudentId()));
                        continue;
                    }
                    member.restorePayment(payment);
                } catch (InvalidInputException ex) {
                    warnings.add(String.format("%s line %d skipped: %s",
                            paymentsFile.getFileName(), lineNum, ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            warnings.add(String.format("Could not read %s: %s",
                    paymentsFile.getFileName(), ex.getMessage()));
        }
    }

    private void loadFines(List<String> warnings) {
        if (!Files.exists(finesFile)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(finesFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Fine fine = Fine.fromCsv(line);
                    if (!membersById.containsKey(fine.getStudentId())) {
                        warnings.add(String.format("%s line %d skipped: no member found with ID '%s'.",
                                finesFile.getFileName(), lineNum, fine.getStudentId()));
                        continue;
                    }
                    boolean duplicateFine = false;
                    for (Fine existing : fines) {
                        if (existing.getFineId().equals(fine.getFineId())) {
                            duplicateFine = true;
                            break;
                        }
                    }
                    if (duplicateFine) {
                        warnings.add(String.format("%s line %d skipped: duplicate fine ID '%s'.",
                                finesFile.getFileName(), lineNum, fine.getFineId()));
                        continue;
                    }
                    fines.add(fine);
                } catch (InvalidInputException ex) {
                    warnings.add(String.format("%s line %d skipped: %s",
                            finesFile.getFileName(), lineNum, ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            warnings.add(String.format("Could not read %s: %s",
                    finesFile.getFileName(), ex.getMessage()));
        }
    }

    @Override
    public void save() {
        try {
            ensureParentDirectory(membersFile);
            ensureParentDirectory(paymentsFile);
            ensureParentDirectory(finesFile);

            try (BufferedWriter memberWriter = Files.newBufferedWriter(membersFile, StandardCharsets.UTF_8);
                 BufferedWriter paymentWriter = Files.newBufferedWriter(paymentsFile, StandardCharsets.UTF_8)) {
                for (Member member : membersById.values()) {
                    memberWriter.write(member.toCsv());
                    memberWriter.newLine();

                    for (Payment payment : member.getPayments()) {
                        paymentWriter.write(payment.toCsv());
                        paymentWriter.newLine();
                    }
                }
            }

            try (BufferedWriter fineWriter = Files.newBufferedWriter(finesFile, StandardCharsets.UTF_8)) {
                for (Fine fine : fines) {
                    fineWriter.write(fine.toCsv());
                    fineWriter.newLine();
                }
            }
        } catch (IOException ex) {
            throw new InvalidInputException("Failed to save data files: " + ex.getMessage());
        }
    }

    private void ensureParentDirectory(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}