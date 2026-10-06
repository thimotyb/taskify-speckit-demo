package com.taskify.project.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Controller tests for the public read endpoints: users and projects (FR-001, FR-003, FR-004, FR-017). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReadEndpointsTest {

    static final String PRIYA = "00000000-0000-0000-0000-000000000001";
    static final String PROJECT_1 = "10000000-0000-0000-0000-000000000001";

    @Autowired
    MockMvc mvc;

    @Test
    void listsExactlyFivePredefinedUsersWithoutIdentity() throws Exception {
        // the user list is public: a client needs it to choose an identity
        mvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[?(@.role == 'PRODUCT_MANAGER')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.role == 'ENGINEER')]", hasSize(4)));
    }

    @Test
    void listsThreeSeededProjects() throws Exception {
        mvc.perform(get("/api/v1/projects").header("X-User-Id", PRIYA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("Website Redesign")))
                .andExpect(jsonPath("$[1].name", is("Mobile App Launch")))
                .andExpect(jsonPath("$[2].name", is("Internal Tooling")));
    }

    @Test
    void getsOneProject() throws Exception {
        mvc.perform(get("/api/v1/projects/" + PROJECT_1).header("X-User-Id", PRIYA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(PROJECT_1)))
                .andExpect(jsonPath("$.name", is("Website Redesign")));
    }

    @Test
    void unknownProjectIs404() throws Exception {
        mvc.perform(get("/api/v1/projects/99999999-0000-0000-0000-000000000000").header("X-User-Id", PRIYA))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void malformedProjectIdIs404() throws Exception {
        mvc.perform(get("/api/v1/projects/not-a-uuid").header("X-User-Id", PRIYA))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingUserHeaderIs401() throws Exception {
        mvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/api/v1/projects/" + PROJECT_1)).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownOrMalformedUserIs401() throws Exception {
        mvc.perform(get("/api/v1/projects").header("X-User-Id", "99999999-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/projects").header("X-User-Id", "not-a-uuid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void errorBodiesNeverLeakInternals() throws Exception {
        mvc.perform(get("/api/v1/projects"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
