package io.leelakrishna.jobtrack.api;

import io.leelakrishna.jobtrack.api.dto.ApplicationResponse;
import io.leelakrishna.jobtrack.api.dto.CreateApplicationRequest;
import io.leelakrishna.jobtrack.api.dto.UpdateStatusRequest;
import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.service.JobApplicationService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse body = ApplicationResponse.from(
                service.create(request.company(), request.role(), request.canonicalUrl(), request.matchScore()));
        return ResponseEntity.created(URI.create("/api/v1/applications/" + body.id())).body(body);
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id) {
        return ApplicationResponse.from(service.get(id));
    }

    @GetMapping
    public Page<ApplicationResponse> list(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.list(status, pageable).map(ApplicationResponse::from);
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse changeStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return ApplicationResponse.from(service.changeStatus(id, request.status()));
    }
}
