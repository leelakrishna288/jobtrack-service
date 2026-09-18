package io.leelakrishna.jobtrack.api.dto;

import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record ApplicationResponse(
        Long id,
        String company,
        String role,
        String canonicalUrl,
        ApplicationStatus status,
        Set<ApplicationStatus> allowedNext,
        Integer matchScore,
        LocalDate appliedOn,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static ApplicationResponse from(JobApplication entity) {
        return new ApplicationResponse(
                entity.getId(),
                entity.getCompany(),
                entity.getRole(),
                entity.getCanonicalUrl(),
                entity.getStatus(),
                entity.getStatus().allowedNext(),
                entity.getMatchScore(),
                entity.getAppliedOn(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }
}
