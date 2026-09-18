package io.leelakrishna.jobtrack.repo;

import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Optional<JobApplication> findByCanonicalUrl(String canonicalUrl);

    boolean existsByCanonicalUrl(String canonicalUrl);

    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
