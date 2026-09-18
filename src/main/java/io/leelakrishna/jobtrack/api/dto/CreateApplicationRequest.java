package io.leelakrishna.jobtrack.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotBlank @Size(max = 200) String company,
        @NotBlank @Size(max = 200) String role,
        @NotBlank
        @Size(max = 1000)
        @Pattern(regexp = "^https://.*", message = "must be an https URL")
        String canonicalUrl,
        @Min(0) @Max(100) Integer matchScore) {
}
