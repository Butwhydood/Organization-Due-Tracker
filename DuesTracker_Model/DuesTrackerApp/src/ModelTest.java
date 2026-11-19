import exceptions.InvalidInputException;
import exceptions.OverpaymentException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.DuesReport;
import model.Fine;
import model.Member;
import model.Officer;
import model.Payable;
import model.Payment;
import model.RegularMember;
import model.Validator;

/**
 * Console test for the model layer (milestone M2). It needs no window and no
 * files. Run it and check that every line marked CHECK prints PASS.
 */
public class ModelTest {

    private static int failures = 0;

    /**
     * Runs every model test and prints a summary.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        testPolymorphicLoop();
        testPayments();
        testValidation();
        testCsvRoundTrip();
        testDamagedLines();
        testPayableMix();

        System.out.println();
        System.out.println(failures == 0 ? "ALL CHECKS PASSED" : failures + " CHECK(S) FAILED");
    }

    private static void testPolymorphicLoop() {
        System.out.println("== 1. Polymorphism: one loop over List<Member> ==");
        List<Member> members = new ArrayList<>();
        members.add(new RegularMember("2024-0001", "Ana Reyes", "BSCS", 2));
        members.add(new Officer("2023-0002", "Ben Santos", "BSCS", 3, "Treasurer"));
        members.add(new Officer("2023-0003", "Carla Lim", "BSIT", 4));   // overloaded constructor

        for (Member member : members) {
            System.out.println(member + " | due PHP " + member.getDuesAmount());
        }
        check("regular dues are 200", members.get(0).getDuesAmount() == 200.00);
        check("officer dues are 100", members.get(1).getDuesAmount() == 100.00);
        check("default officer position", ((Officer) members.get(2)).getPosition().equals("Officer"));
    }

    private static void testPayments() {
        System.out.println("\n== 2. Payments and the overpayment rule ==");
        Member ana = new RegularMember("2024-0001", "Ana Reyes", "BSCS", 2);
        try {
            ana.pay(50);                                   // pay(double)
            ana.pay(100, "Second installment");            // pay(double, String)
            ana.pay(25, "Old receipt", LocalDate.of(2026, 9, 1)); // pay(double, String, LocalDate)
        } catch (OverpaymentException e) {
            check("no overpayment expected here", false);
        }
        check("paid 175", ana.getAmountPaid() == 175.00);
        check("balance 25", ana.getBalance() == 25.00);
        check("status PARTIAL", ana.getStatus().equals("PARTIAL"));

        try {
            ana.pay(30);
            check("overpayment should be rejected", false);
        } catch (OverpaymentException e) {
            System.out.println("Rejected: " + e.getMessage());
            check("overpayment rejected with balance 25", e.getRemainingBalance() == 25.00);
        }
        try {
            ana.pay(25);
        } catch (OverpaymentException e) {
            check("exact balance should be accepted", false);
        }
        check("status PAID", ana.getStatus().equals("PAID") && ana.isFullyPaid());
        check("4 payments recorded", ana.getPayments().size() == 4);
    }

    private static void testValidation() {
        System.out.println("\n== 3. Validation: every bad value must be rejected ==");
        expectRejected("empty name", () -> new RegularMember("2024-0010", "  ", "BSCS", 1));
        expectRejected("null course", () -> new RegularMember("2024-0010", "Dan Uy", null, 1));
        expectRejected("year level 0", () -> new RegularMember("2024-0010", "Dan Uy", "BSCS", 0));
        expectRejected("year level 9", () -> new RegularMember("2024-0010", "Dan Uy", "BSCS", 9));
        expectRejected("bad ID characters", () -> new RegularMember("20 24!", "Dan Uy", "BSCS", 1));
        expectRejected("comma in name", () -> new RegularMember("2024-0010", "Uy, Dan", "BSCS", 1));
        expectRejected("empty position", () -> new Officer("2024-0011", "Eva Go", "BSCS", 2, ""));
        expectRejected("letters in year box", () -> Validator.parseInt("Year level", "two"));
        expectRejected("letters in amount box", () -> Validator.parseAmount("Amount", "abc"));
        expectRejected("negative amount", () -> Validator.parseAmount("Amount", "-5"));
        expectRejected("zero amount", () -> Validator.parseAmount("Amount", "0"));
        expectRejected("future payment date", () ->
                new Payment("2024-0010", 10, LocalDate.now().plusDays(3), "x"));

        Member dan = new RegularMember("2024-0010", "Dan Uy", "BSCS", 1);
        expectRejected("setter rejects blank name", () -> dan.setFullName(""));
        check("name unchanged after rejected setter", dan.getFullName().equals("Dan Uy"));
    }

    private static void testCsvRoundTrip() {
        System.out.println("\n== 4. CSV round trip ==");
        Member officer = new Officer("2023-0002", "Ben Santos", "BSCS", 3, "Treasurer");
        String line = officer.toCsv();
        System.out.println(line);
        Member copy = Member.fromCsv(line);
        check("officer comes back as Officer", copy instanceof Officer);
        check("officer fields equal", copy.toCsv().equals(line));

        Member regular = Member.fromCsv("REGULAR,2024-0001,Ana Reyes,BSCS,2");
        check("regular comes back as RegularMember", regular instanceof RegularMember);

        Payment payment = new Payment("2024-0001", 150.5, LocalDate.of(2026, 9, 15), "Cash");
        System.out.println(payment.toCsv());
        check("payment round trip", Payment.fromCsv(payment.toCsv()).toCsv().equals(payment.toCsv()));

        Fine fine = new Fine("F-001", "2024-0001", "Missed general assembly", 50, 20);
        System.out.println(fine.toCsv());
        check("fine round trip", Fine.fromCsv(fine.toCsv()).toCsv().equals(fine.toCsv()));
    }

    private static void testDamagedLines() {
        System.out.println("\n== 5. Damaged lines are rejected with a message, never a crash ==");
        String[] damagedMembers = {
            "", "REGULAR,2024-0001,Ana Reyes,BSCS", "REGULAR,2024-0001,Ana Reyes,BSCS,two",
            "ALUMNI,2024-0001,Ana Reyes,BSCS,2", "OFFICER,2024-0001,Ana Reyes,BSCS,2",
            "REGULAR,2024-0001,Ana Reyes,BSCS,99"
        };
        for (String line : damagedMembers) {
            expectRejected("member line [" + line + "]", () -> Member.fromCsv(line));
        }
        expectRejected("payment with letters", () -> Payment.fromCsv("2024-0001,abc,2026-09-15,Cash"));
        expectRejected("payment with bad date", () -> Payment.fromCsv("2024-0001,50.00,15/09/2026,Cash"));
        expectRejected("fine paid more than amount", () -> Fine.fromCsv("F-001,2024-0001,Late,50.00,80.00"));
    }

    private static void testPayableMix() {
        System.out.println("\n== 6. Interface: Members and Fines in one List<Payable> ==");
        Member ana = new RegularMember("2024-0001", "Ana Reyes", "BSCS", 2);
        Member ben = new Officer("2023-0002", "Ben Santos", "BSCS", 3, "Treasurer");
        Fine fine = new Fine("F-001", "2024-0001", "Missed assembly", 50);
        try {
            ana.pay(200);
            ben.pay(40);
            fine.pay(10);
        } catch (OverpaymentException e) {
            check("no overpayment expected here", false);
        }
        List<Payable> everything = new ArrayList<>(List.of(ana, ben, fine));
        System.out.println("Total due:         PHP " + DuesReport.totalDue(everything));
        System.out.println("Total collected:   PHP " + DuesReport.totalCollected(everything));
        System.out.println("Total outstanding: PHP " + DuesReport.totalOutstanding(everything));
        check("total due 350", DuesReport.totalDue(everything) == 350.00);
        check("total collected 250", DuesReport.totalCollected(everything) == 250.00);
        check("total outstanding 100", DuesReport.totalOutstanding(everything) == 100.00);
        check("2 items still unpaid", DuesReport.unpaid(everything).size() == 2);
    }

    // ----- tiny helpers so the test stays readable -----

    private static void check(String label, boolean condition) {
        System.out.println("CHECK " + (condition ? "PASS" : "FAIL") + ": " + label);
        if (!condition) {
            failures++;
        }
    }

    private static void expectRejected(String label, Runnable action) {
        try {
            action.run();
            check(label + " should be rejected", false);
        } catch (InvalidInputException e) {
            System.out.println("CHECK PASS: " + label + " -> \"" + e.getMessage() + "\"");
        }
    }
}
