package io.leelakrishna.jobtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "job_application")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String company;

    @Column(name = "job_role", nullable = false, length = 200)
    private String role;

    @Column(name = "canonical_url", nullable = false, length = 1000, unique = true)
    private String canonicalUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus status = ApplicationStatus.DISCOVERED;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "applied_on")
    private LocalDate appliedOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /** Optimistic locking: two concurrent status changes must not silently overwrite each other. */
    @Version
    @Column(nullable = false)
    private long version;

    protected JobApplication() {
        // required by JPA
    }

    public JobApplication(String company, String role, String canonicalUrl, Integer matchScore) {
        this.company = Objects.requireNonNull(company, "company");
        this.role = Objects.requireNonNull(role, "role");
        this.canonicalUrl = Objects.requireNonNull(canonicalUrl, "canonicalUrl");
        this.matchScore = matchScore;
    }

    /**
     * Move to {@code next}, or refuse if the funnel does not allow it.
     *
     * @throws IllegalStateException when the transition is not permitted
     */
    public void moveTo(ApplicationStatus next) {
        Objects.requireNonNull(next, "next");
        if (next == this.status) {
            return;
        }
        if (!this.status.canMoveTo(next)) {
            throw new IllegalStateException("cannot move from " + this.status + " to " + next);
        }
        this.status = next;
        if (next == ApplicationStatus.SUBMITTED && this.appliedOn == null) {
            this.appliedOn = LocalDate.now();
        }
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getRole() {
        return role;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public LocalDate getAppliedOn() {
        return appliedOn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
