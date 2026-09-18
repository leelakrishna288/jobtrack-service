package io.leelakrishna.jobtrack.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ApplicationStatusTest {

    @ParameterizedTest
    @CsvSource({
        "DISCOVERED,RESUME_PREPARED,true",
        "DISCOVERED,SUBMITTED,false",
        "RESUME_PREPARED,APPROVED,true",
        "RESUME_PREPARED,INTERVIEW,false",
        "APPROVED,SUBMITTED,true",
        "APPROVED,REJECTED,false",
        "SUBMITTED,INTERVIEW,true",
        "INTERVIEW,OFFER,true",
        "OFFER,INTERVIEW,false",
        "REJECTED,SUBMITTED,false",
        "WITHDRAWN,DISCOVERED,false"
    })
    void transitionRules(ApplicationStatus from, ApplicationStatus to, boolean allowed) {
        assertThat(from.canMoveTo(to)).isEqualTo(allowed);
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"OFFER", "REJECTED", "WITHDRAWN"})
    void terminalStatesAllowNothing(ApplicationStatus terminal) {
        assertThat(terminal.isTerminal()).isTrue();
        assertThat(terminal.allowedNext()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(
            value = ApplicationStatus.class,
            names = {"OFFER", "REJECTED", "WITHDRAWN"},
            mode = EnumSource.Mode.EXCLUDE)
    void everyNonTerminalStateCanBeWithdrawn(ApplicationStatus status) {
        assertThat(status.canMoveTo(ApplicationStatus.WITHDRAWN)).isTrue();
    }

    @Test
    void movingToSubmittedStampsTheAppliedDate() {
        JobApplication application = new JobApplication("S-PRO", "AI Engineer", "https://x/1", 85);
        application.moveTo(ApplicationStatus.RESUME_PREPARED);
        application.moveTo(ApplicationStatus.APPROVED);
        assertThat(application.getAppliedOn()).isNull();

        application.moveTo(ApplicationStatus.SUBMITTED);

        assertThat(application.getAppliedOn()).isEqualTo(LocalDate.now());
    }

    @Test
    void movingToTheSameStatusIsANoOp() {
        JobApplication application = new JobApplication("S-PRO", "AI Engineer", "https://x/1", 85);
        application.moveTo(ApplicationStatus.DISCOVERED);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.DISCOVERED);
    }

    @Test
    void anInvalidTransitionIsRefusedWithBothStatesNamed() {
        JobApplication application = new JobApplication("S-PRO", "AI Engineer", "https://x/1", 85);
        assertThatThrownBy(() -> application.moveTo(ApplicationStatus.OFFER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DISCOVERED")
                .hasMessageContaining("OFFER");
    }
}
