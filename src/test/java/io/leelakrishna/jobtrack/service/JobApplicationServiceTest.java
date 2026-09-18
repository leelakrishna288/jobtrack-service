package io.leelakrishna.jobtrack.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import io.leelakrishna.jobtrack.repo.JobApplicationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @InjectMocks
    private JobApplicationService service;

    @Test
    void createRejectsADuplicateCanonicalUrlWithoutSaving() {
        when(repository.existsByCanonicalUrl("https://x/1")).thenReturn(true);

        assertThatThrownBy(() -> service.create("S-PRO", "AI Engineer", "https://x/1", 85))
                .isInstanceOf(DuplicateApplicationException.class)
                .hasMessageContaining("https://x/1");

        verify(repository, never()).save(any());
    }

    @Test
    void createSavesWhenTheUrlIsNew() {
        when(repository.existsByCanonicalUrl("https://x/2")).thenReturn(false);
        JobApplication entity = new JobApplication("S-PRO", "AI Engineer", "https://x/2", 85);
        when(repository.save(any())).thenReturn(entity);

        JobApplication created = service.create("S-PRO", "AI Engineer", "https://x/2", 85);

        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.DISCOVERED);
        verify(repository).save(any());
    }

    @Test
    void getThrowsWhenMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(ApplicationNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void changeStatusAppliesTheDomainRule() {
        JobApplication entity = new JobApplication("S-PRO", "AI Engineer", "https://x/3", 85);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        JobApplication moved = service.changeStatus(1L, ApplicationStatus.RESUME_PREPARED);

        assertThat(moved.getStatus()).isEqualTo(ApplicationStatus.RESUME_PREPARED);
    }

    @Test
    void changeStatusRefusesAnIllegalJump() {
        JobApplication entity = new JobApplication("S-PRO", "AI Engineer", "https://x/4", 85);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.changeStatus(1L, ApplicationStatus.OFFER))
                .isInstanceOf(IllegalStateException.class);

        assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.DISCOVERED);
    }
}
