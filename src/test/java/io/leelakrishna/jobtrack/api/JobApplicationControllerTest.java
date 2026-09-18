package io.leelakrishna.jobtrack.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.leelakrishna.jobtrack.domain.ApplicationStatus;
import io.leelakrishna.jobtrack.domain.JobApplication;
import io.leelakrishna.jobtrack.service.ApplicationNotFoundException;
import io.leelakrishna.jobtrack.service.DuplicateApplicationException;
import io.leelakrishna.jobtrack.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(JobApplicationController.class)
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000")
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobApplicationService service;

    private static JobApplication sample() {
        return new JobApplication("S-PRO", "AI Engineer", "https://example.test/jobs/1", 85);
    }

    @Test
    void createReturns201WithALocationHeader() throws Exception {
        when(service.create(any(), any(), any(), any())).thenReturn(sample());

        mockMvc.perform(post("/api/v1/applications")
                        .with(jwt().jwt(builder -> builder.claim("scope", "jobtrack.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"S-PRO","role":"AI Engineer",
                                 "canonicalUrl":"https://example.test/jobs/1","matchScore":85}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company").value("S-PRO"))
                .andExpect(jsonPath("$.status").value("DISCOVERED"))
                .andExpect(jsonPath("$.allowedNext").isArray());
    }

    @Test
    void createRejectsANonHttpsUrlWithFieldDetail() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .with(jwt().jwt(builder -> builder.claim("scope", "jobtrack.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"S-PRO","role":"AI Engineer",
                                 "canonicalUrl":"http://insecure.test/jobs/1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.fields.canonicalUrl").exists());
    }

    @Test
    void createRejectsAnOutOfRangeMatchScore() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .with(jwt().jwt(builder -> builder.claim("scope", "jobtrack.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"S-PRO","role":"AI Engineer",
                                 "canonicalUrl":"https://example.test/jobs/1","matchScore":140}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.matchScore").exists());
    }

    @Test
    void duplicateBecomes409() throws Exception {
        when(service.create(any(), any(), any(), any()))
                .thenThrow(new DuplicateApplicationException("https://example.test/jobs/1"));

        mockMvc.perform(post("/api/v1/applications")
                        .with(jwt().jwt(builder -> builder.claim("scope", "jobtrack.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"S-PRO","role":"AI Engineer",
                                 "canonicalUrl":"https://example.test/jobs/1"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate application"));
    }

    @Test
    void missingApplicationBecomes404() throws Exception {
        when(service.get(99L)).thenThrow(new ApplicationNotFoundException(99L));

        mockMvc.perform(get("/api/v1/applications/99")
                        .with(jwt().jwt(builder -> builder.claim("scope", "jobtrack.read"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Application not found"));
    }

    @Test
    void anIllegalTransitionBecomes409NotA500() throws Exception {
        when(service.changeStatus(eq(1L), eq(ApplicationStatus.OFFER)))
                .thenThrow(new IllegalStateException("cannot move from DISCOVERED to OFFER"));

        mockMvc.perform(patch("/api/v1/applications/1/status")
                        .with(jwt().jwt(builder -> builder.claim("scope", "jobtrack.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFER\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid status transition"));
    }

    @Test
    void anUnauthenticatedCallIs401() throws Exception {
        mockMvc.perform(get("/api/v1/applications/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void aWriteScopeIsRequiredToPost() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .with(jwt().jwt(builder -> builder.claim("scope", "jobtrack.read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"S-PRO","role":"AI Engineer",
                                 "canonicalUrl":"https://example.test/jobs/1"}
                                """))
                .andExpect(status().isForbidden());
    }
}
