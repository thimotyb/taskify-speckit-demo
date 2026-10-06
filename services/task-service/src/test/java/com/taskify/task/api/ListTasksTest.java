package com.taskify.task.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskify.task.client.ProjectServiceClient;
import com.taskify.task.client.UpstreamUnavailableException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Controller tests for {@code GET /api/v1/projects/{projectId}/tasks} (FR-006, FR-015, FR-017). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ListTasksTest {

    static final String PRIYA = "00000000-0000-0000-0000-000000000001";
    static final String PROJECT_1 = "10000000-0000-0000-0000-000000000001";

    @Autowired
    MockMvc mvc;

    @MockBean
    ProjectServiceClient projectService;

    @BeforeEach
    void knownUserAndProject() {
        when(projectService.userExists(UUID.fromString(PRIYA))).thenReturn(true);
        when(projectService.projectExists(UUID.fromString(PROJECT_1))).thenReturn(true);
    }

    @Test
    void listsSeededTasksWithCommentCountsOldestFirst() throws Exception {
        mvc.perform(get("/api/v1/projects/" + PROJECT_1 + "/tasks").header("X-User-Id", PRIYA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[*].title", contains("Audit current site content", "Collect brand guidelines",
                        "Design new homepage mockups", "Set up component library", "Write navigation copy",
                        "Accessibility review of mockups", "Define sitemap", "Choose hosting provider")))
                // two tasks in each of the four statuses
                .andExpect(jsonPath("$[?(@.status == 'TODO')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.status == 'IN_PROGRESS')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.status == 'IN_REVIEW')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.status == 'DONE')]", hasSize(2)))
                // comment counts: task 03 has two comments, task 05 has one, the rest none
                .andExpect(jsonPath("$[2].commentCount", is(2)))
                .andExpect(jsonPath("$[4].commentCount", is(1)))
                .andExpect(jsonPath("$[0].commentCount", is(0)))
                // assignee and unassigned
                .andExpect(jsonPath("$[0].assigneeId", is("00000000-0000-0000-0000-000000000002")))
                .andExpect(jsonPath("$[1].assigneeId", nullValue()));
    }

    @Test
    void unknownProjectIs404() throws Exception {
        UUID other = UUID.fromString("99999999-0000-0000-0000-000000000000");
        when(projectService.projectExists(eq(other))).thenReturn(false);
        mvc.perform(get("/api/v1/projects/" + other + "/tasks").header("X-User-Id", PRIYA))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void missingUserIs401() throws Exception {
        mvc.perform(get("/api/v1/projects/" + PROJECT_1 + "/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserIs401() throws Exception {
        when(projectService.userExists(any(UUID.class))).thenReturn(false);
        mvc.perform(get("/api/v1/projects/" + PROJECT_1 + "/tasks")
                .header("X-User-Id", "99999999-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void projectServiceDownFailsClosedWith503() throws Exception {
        when(projectService.userExists(any(UUID.class)))
                .thenThrow(new UpstreamUnavailableException("down", null));
        mvc.perform(get("/api/v1/projects/" + PROJECT_1 + "/tasks").header("X-User-Id", PRIYA))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void projectCheckFailureOnBoardLoadIs503() throws Exception {
        when(projectService.projectExists(UUID.fromString(PROJECT_1)))
                .thenThrow(new UpstreamUnavailableException("down", null));
        mvc.perform(get("/api/v1/projects/" + PROJECT_1 + "/tasks").header("X-User-Id", PRIYA))
                .andExpect(status().isServiceUnavailable());
    }
}
