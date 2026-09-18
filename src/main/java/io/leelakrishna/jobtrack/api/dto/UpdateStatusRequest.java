package io.leelakrishna.jobtrack.api.dto;

import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull ApplicationStatus status) {
}
