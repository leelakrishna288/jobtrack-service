import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import io.leelakrishna.jobtrack.repo.JobApplicationRepository;
import io.leelakrishna.jobtrack.service.ApplicationNotFoundException;
import io.leelakrishna.jobtrack.service.DuplicateApplicationException;
import io.leelakrishna.jobtrack.service.JobApplicationService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Runs JobApplicationService against a hand-written fake repository - real behaviour, no Mockito. */
public class ServiceHarness {

    static int pass = 0;
    static int fail = 0;
    static final List<String> failures = new ArrayList<>();

    static void check(String name, boolean ok) {
        if (ok) { pass++; } else { fail++; failures.add(name); }
    }

    static final class FakePage<T> implements Page<T> {
        final List<T> items;
        FakePage(List<T> items) { this.items = items; }
        public <U> Page<U> map(Function<? super T, ? extends U> converter) {
            List<U> out = new ArrayList<>();
            for (T t : items) { out.add(converter.apply(t)); }
            return new FakePage<>(out);
        }
        int size() { return items.size(); }
    }

    static final class FakeRepo implements JobApplicationRepository {
        final Map<Long, JobApplication> rows = new LinkedHashMap<>();
        long nextId = 1;
        int saveCalls = 0;

        public JobApplication save(JobApplication entity) {
            saveCalls++;
            rows.put(nextId++, entity);
            return entity;
        }
        public JobApplication saveAndFlush(JobApplication entity) { return save(entity); }
        public Optional<JobApplication> findById(Long id) { return Optional.ofNullable(rows.get(id)); }
        public Page<JobApplication> findAll(Pageable pageable) { return new FakePage<>(new ArrayList<>(rows.values())); }
        public Optional<JobApplication> findByCanonicalUrl(String url) {
            return rows.values().stream().filter(r -> r.getCanonicalUrl().equals(url)).findFirst();
        }
        public boolean existsByCanonicalUrl(String url) { return findByCanonicalUrl(url).isPresent(); }
        public Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable) {
            List<JobApplication> hits = new ArrayList<>();
            for (JobApplication r : rows.values()) { if (r.getStatus() == status) { hits.add(r); } }
            return new FakePage<>(hits);
        }
    }

    public static void main(String[] args) {
        FakeRepo repo = new FakeRepo();
        JobApplicationService service = new JobApplicationService(repo);

        JobApplication created = service.create("S-PRO", "AI Engineer", "https://x/1", 85);
        check("create returns the saved entity", created.getCompany().equals("S-PRO"));
        check("create starts at DISCOVERED", created.getStatus() == ApplicationStatus.DISCOVERED);
        check("create persisted one row", repo.saveCalls == 1);

        try {
            service.create("Other", "Other", "https://x/1", 60);
            check("duplicate URL refused", false);
        } catch (DuplicateApplicationException e) {
            check("duplicate URL refused", true);
            check("duplicate message names the url", e.getMessage().contains("https://x/1"));
            check("duplicate did not save", repo.saveCalls == 1);
        }

        check("get returns the row", service.get(1L).getCanonicalUrl().equals("https://x/1"));
        try {
            service.get(999L);
            check("missing id throws", false);
        } catch (ApplicationNotFoundException e) {
            check("missing id throws", true);
            check("not-found message names the id", e.getMessage().contains("999"));
        }

        JobApplication moved = service.changeStatus(1L, ApplicationStatus.RESUME_PREPARED);
        check("changeStatus applies the move", moved.getStatus() == ApplicationStatus.RESUME_PREPARED);
        try {
            service.changeStatus(1L, ApplicationStatus.OFFER);
            check("illegal transition refused by service", false);
        } catch (IllegalStateException e) {
            check("illegal transition refused by service", true);
            check("state unchanged after refusal",
                    service.get(1L).getStatus() == ApplicationStatus.RESUME_PREPARED);
        }

        service.create("B", "r", "https://x/2", 70);
        check("list without filter returns all", ((FakePage<JobApplication>) service.list(null, null)).size() == 2);
        check("list filtered by status",
                ((FakePage<JobApplication>) service.list(ApplicationStatus.RESUME_PREPARED, null)).size() == 1);
        check("list filter excludes others",
                ((FakePage<JobApplication>) service.list(ApplicationStatus.OFFER, null)).size() == 0);

        // full happy path through the funnel
        JobApplication f = service.create("Lloyds", "SE II", "https://x/3", 82);
        service.changeStatus(3L, ApplicationStatus.RESUME_PREPARED);
        service.changeStatus(3L, ApplicationStatus.APPROVED);
        service.changeStatus(3L, ApplicationStatus.SUBMITTED);
        check("funnel reaches SUBMITTED", f.getStatus() == ApplicationStatus.SUBMITTED);
        check("appliedOn stamped by the service path", f.getAppliedOn() != null);
        service.changeStatus(3L, ApplicationStatus.INTERVIEW);
        service.changeStatus(3L, ApplicationStatus.OFFER);
        check("funnel reaches OFFER", f.getStatus() == ApplicationStatus.OFFER);
        check("OFFER is terminal", f.getStatus().isTerminal());

        System.out.println(pass + " service checks passed, " + fail + " failed");
        failures.forEach(x -> System.out.println("  FAIL: " + x));
        if (fail > 0) { System.exit(1); }
    }
}
