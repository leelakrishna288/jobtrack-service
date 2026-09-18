package io.leelakrishna.jobtrack.service;

import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import io.leelakrishna.jobtrack.repo.JobApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationService.class);

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    /**
     * Create an application, refusing a duplicate of the same canonical URL.
     *
     * <p>The unique constraint on the column is the real guard; this check exists so the caller
     * gets a 409 with a useful message instead of a constraint-violation stack trace.
     */
    @Transactional
    public JobApplication create(String company, String role, String canonicalUrl, Integer matchScore) {
        if (repository.existsByCanonicalUrl(canonicalUrl)) {
            throw new DuplicateApplicationException(canonicalUrl);
        }
        JobApplication saved = repository.save(new JobApplication(company, role, canonicalUrl, matchScore));
        log.info("created application id={} company={} status={}", saved.getId(), company, saved.getStatus());
        return saved;
    }

    @Transactional(readOnly = true)
    public JobApplication get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<JobApplication> list(ApplicationStatus status, Pageable pageable) {
        return status == null
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
    }

    /**
     * Move an application to {@code next}. The transition rule lives on the entity, so an
     * invalid move is refused here exactly as it would be anywhere else.
     */
    @Transactional
    public JobApplication changeStatus(Long id, ApplicationStatus next) {
        JobApplication application = get(id);
        ApplicationStatus previous = application.getStatus();
        application.moveTo(next);
        log.info("application id={} moved {} -> {}", id, previous, application.getStatus());
        return application;
    }
}
