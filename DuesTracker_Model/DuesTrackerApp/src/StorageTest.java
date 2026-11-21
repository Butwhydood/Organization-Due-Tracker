import exceptions.DuplicateIdException;
import exceptions.MemberNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.Fine;
import model.Officer;
import model.RegularMember;
import storage.FileMemberStore;
import storage.MemberStore;

/**
 * Console verification for custom exceptions and {@link FileMemberStore}.
 */
public class StorageTest {

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("duestracker_storage_test_");
        Path membersCsv = tempDir.resolve("members.csv");
        Path paymentsCsv = tempDir.resolve("payments.csv");
        Path finesCsv = tempDir.resolve("fines.csv");

        MemberStore store = new FileMemberStore(membersCsv, paymentsCsv, finesCsv);

        // 1. First run with missing files should not crash
        List<String> firstLoadWarnings = store.load();
        assert firstLoadWarnings.isEmpty() : "Expected no warnings on missing files";

        // 2. Add members & test DuplicateIdException + MemberNotFoundException
        RegularMember m1 = new RegularMember("2024-0001", "Ana Cruz", "BSCS", 2);
        m1.pay(100.00, "Partial dues");
        Officer m2 = new Officer("2023-0002", "Ben Santos", "BSCS", 3, "Treasurer");

        store.add(m1);
        store.add(m2);

        assertThrowsDuplicateMember(store, new RegularMember("2024-0001", "Other Name", "BSIT", 1));
        assertThrowsMemberNotFound(store, "9999-9999");

        // 3. Add fine & test duplicate fine ID
        Fine f1 = new Fine("F-001", "2024-0001", "Missed assembly", 50.00);
        store.addFine(f1);
        assertThrowsDuplicateFine(store, new Fine("F-001", "2023-0002", "Duplicate fine", 25.00));

        // 4. Save and reload
        store.save();
        MemberStore reloaded = new FileMemberStore(membersCsv, paymentsCsv, finesCsv);
        List<String> reloadWarnings = reloaded.load();
        assert reloadWarnings.isEmpty() : "Unexpected warnings: " + reloadWarnings;
        assert reloaded.getAll().size() == 2 : "Expected 2 members";
        assert reloaded.getFines().size() == 1 : "Expected 1 fine";
        assert reloaded.findById("2024-0001").getAmountPaid() == 100.00 : "Expected restored payment";

        // 5. Append a damaged member line and verify it is skipped + reported
        String existing = Files.readString(membersCsv);
        Files.writeString(membersCsv, existing + "BAD_TYPE,2024-0009,Broken,BSCS,2\n");
        List<String> damagedWarnings = reloaded.load();
        assert damagedWarnings.size() == 1 : "Expected 1 warning for damaged line";
        assert reloaded.getAll().size() == 2 : "Good rows should still load";

        System.out.println("STORAGE AND EXCEPTION CHECKS PASSED");
    }

    private static void assertThrowsDuplicateMember(MemberStore store, model.Member dup) {
        try {
            store.add(dup);
            throw new AssertionError("Expected DuplicateIdException for member");
        } catch (DuplicateIdException expected) {
            assert expected.getMessage().contains("2024-0001");
        }
    }

    private static void assertThrowsDuplicateFine(MemberStore store, Fine dup) throws MemberNotFoundException {
        try {
            store.addFine(dup);
            throw new AssertionError("Expected DuplicateIdException for fine");
        } catch (DuplicateIdException expected) {
            assert expected.getMessage().contains("F-001");
        }
    }

    private static void assertThrowsMemberNotFound(MemberStore store, String missingId) {
        try {
            store.findById(missingId);
            throw new AssertionError("Expected MemberNotFoundException");
        } catch (MemberNotFoundException expected) {
            assert expected.getMessage().contains(missingId);
        }
    }
}