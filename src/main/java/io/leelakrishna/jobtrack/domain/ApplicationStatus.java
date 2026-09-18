package io.leelakrishna.jobtrack.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of one job application.
 *
 * <p>Transitions live in the domain, not in the controller: an application moves forward
 * through the funnel or sideways into a terminal state, and nothing else is allowed. Keeping
 * the rule here means an invalid transition is rejected identically whichever entry point
 * asks for it.
 */
public enum ApplicationStatus {

    DISCOVERED,
    RESUME_PREPARED,
    APPROVED,
    SUBMITTED,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    private static final Set<ApplicationStatus> TERMINAL = EnumSet.of(OFFER, REJECTED, WITHDRAWN);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    /** States this one may move to. Terminal states may not move at all. */
    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case DISCOVERED -> EnumSet.of(RESUME_PREPARED, REJECTED, WITHDRAWN);
            case RESUME_PREPARED -> EnumSet.of(APPROVED, REJECTED, WITHDRAWN);
            case APPROVED -> EnumSet.of(SUBMITTED, WITHDRAWN);
            case SUBMITTED -> EnumSet.of(INTERVIEW, REJECTED, WITHDRAWN);
            case INTERVIEW -> EnumSet.of(OFFER, REJECTED, WITHDRAWN);
            case OFFER, REJECTED, WITHDRAWN -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canMoveTo(ApplicationStatus next) {
        return allowedNext().contains(next);
    }
}
