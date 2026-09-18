package io.leelakrishna.jobtrack.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import io.leelakrishna.jobtrack.repo.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

/** Persistence slice against H2, so the mapping and the unique constraint are actually exercised. */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository repository;

    @Test
    void savesAndReadsBackByCanonicalUrl() {
        repository.saveAndFlush(new JobApplication("S-PRO", "AI Engineer", "https://example.test/1", 85));

        assertThat(repository.findByCanonicalUrl("https://example.test/1"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getCompany()).isEqualTo("S-PRO");
                    assertThat(found.getStatus()).isEqualTo(ApplicationStatus.DISCOVERED);
                    assertThat(found.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void theUniqueConstraintRejectsADuplicateUrl() {
        repository.saveAndFlush(new JobApplication("S-PRO", "AI Engineer", "https://example.test/2", 85));

        assertThatThrownBy(() -> repository.saveAndFlush(
                        new JobApplication("Other", "Other role", "https://example.test/2", 60)))
                .isInstanceOf(Exception.class);
    }

    @Test
    void filtersByStatusWithPaging() {
        repository.saveAndFlush(new JobApplication("A", "r", "https://example.test/3", 70));
        JobApplication submitted = new JobApplication("B", "r", "https://example.test/4", 80);
        submitted.moveTo(ApplicationStatus.RESUME_PREPARED);
        submitted.moveTo(ApplicationStatus.APPROVED);
        submitted.moveTo(ApplicationStatus.SUBMITTED);
        repository.saveAndFlush(submitted);

        assertThat(repository.findByStatus(ApplicationStatus.SUBMITTED, PageRequest.of(0, 10)).getContent())
                .hasSize(1)
                .allSatisfy(found -> assertThat(found.getAppliedOn()).isNotNull());
    }
}
