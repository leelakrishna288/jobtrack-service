import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import java.time.LocalDate;
import java.util.List;

/** Runs the domain rules for real (no JUnit available offline) to verify behaviour, not just syntax. */
public class DomainHarness {
    static int pass = 0;
    static int fail = 0;

    static void check(String name, boolean ok) {
        if (ok) { pass++; } else { fail++; System.out.println("  FAIL: " + name); }
    }

    public static void main(String[] args) {
        // transition table, mirroring the parameterised JUnit cases
        record Case(ApplicationStatus from, ApplicationStatus to, boolean allowed) {}
        List<Case> cases = List.of(
            new Case(ApplicationStatus.DISCOVERED, ApplicationStatus.RESUME_PREPARED, true),
            new Case(ApplicationStatus.DISCOVERED, ApplicationStatus.SUBMITTED, false),
            new Case(ApplicationStatus.RESUME_PREPARED, ApplicationStatus.APPROVED, true),
            new Case(ApplicationStatus.RESUME_PREPARED, ApplicationStatus.INTERVIEW, false),
            new Case(ApplicationStatus.APPROVED, ApplicationStatus.SUBMITTED, true),
            new Case(ApplicationStatus.APPROVED, ApplicationStatus.REJECTED, false),
            new Case(ApplicationStatus.SUBMITTED, ApplicationStatus.INTERVIEW, true),
            new Case(ApplicationStatus.INTERVIEW, ApplicationStatus.OFFER, true),
            new Case(ApplicationStatus.OFFER, ApplicationStatus.INTERVIEW, false),
            new Case(ApplicationStatus.REJECTED, ApplicationStatus.SUBMITTED, false),
            new Case(ApplicationStatus.WITHDRAWN, ApplicationStatus.DISCOVERED, false));
        for (Case c : cases) {
            check(c.from() + "->" + c.to(), c.from().canMoveTo(c.to()) == c.allowed());
        }

        for (ApplicationStatus s : ApplicationStatus.values()) {
            boolean terminal = s == ApplicationStatus.OFFER || s == ApplicationStatus.REJECTED
                    || s == ApplicationStatus.WITHDRAWN;
            check("isTerminal " + s, s.isTerminal() == terminal);
            check("terminal has no next " + s, s.allowedNext().isEmpty() == terminal);
            if (!terminal) {
                check("withdrawable " + s, s.canMoveTo(ApplicationStatus.WITHDRAWN));
            }
        }

        // appliedOn is stamped exactly when SUBMITTED is first reached
        JobApplication a = new JobApplication("S-PRO", "AI Engineer", "https://x/1", 85);
        check("starts DISCOVERED", a.getStatus() == ApplicationStatus.DISCOVERED);
        check("appliedOn null initially", a.getAppliedOn() == null);
        a.moveTo(ApplicationStatus.RESUME_PREPARED);
        a.moveTo(ApplicationStatus.APPROVED);
        check("still null before submit", a.getAppliedOn() == null);
        a.moveTo(ApplicationStatus.SUBMITTED);
        check("appliedOn stamped today", LocalDate.now().equals(a.getAppliedOn()));
        LocalDate stamped = a.getAppliedOn();
        a.moveTo(ApplicationStatus.INTERVIEW);
        check("appliedOn not overwritten later", stamped.equals(a.getAppliedOn()));

        // same-status move is a no-op
        JobApplication b = new JobApplication("X", "r", "https://x/2", 70);
        b.moveTo(ApplicationStatus.DISCOVERED);
        check("same-status no-op", b.getStatus() == ApplicationStatus.DISCOVERED);

        // illegal jump refused, state unchanged, both states named
        try {
            b.moveTo(ApplicationStatus.OFFER);
            check("illegal jump throws", false);
        } catch (IllegalStateException e) {
            check("illegal jump throws", true);
            check("message names both states",
                    e.getMessage().contains("DISCOVERED") && e.getMessage().contains("OFFER"));
            check("state unchanged after refusal", b.getStatus() == ApplicationStatus.DISCOVERED);
        }

        // constructor rejects nulls
        try {
            new JobApplication(null, "r", "https://x/3", 1);
            check("null company rejected", false);
        } catch (NullPointerException e) {
            check("null company rejected", true);
        }
        try {
            b.moveTo(null);
            check("null target rejected", false);
        } catch (NullPointerException e) {
            check("null target rejected", true);
        }

        System.out.println(pass + " checks passed, " + fail + " failed");
        if (fail > 0) { System.exit(1); }
    }
}
