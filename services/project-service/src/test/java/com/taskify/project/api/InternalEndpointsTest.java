package com.taskify.project.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Controller tests for the service-token-protected internal endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InternalEndpointsTest {

    static final String TOKEN = "test-service-token-0123456789abcdef0123456789";
    static final String PROJECT_1 = "10000000-0000-0000-0000-000000000001";
    static final String MARCO = "00000000-0000-0000-0000-000000000002";

    @Autowired
    MockMvc mvc;

    @Test
    void projectExists() throws Exception {
        mvc.perform(get("/internal/projects/" + PROJECT_1).header("X-Service-Token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(PROJECT_1)));
    }

    @Test
    void userExists() throws Exception {
        mvc.perform(get("/internal/users/" + MARCO).header("X-Service-Token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Marco Rossi")));
    }

    @Test
    void unknownProjectAndUserAre404() throws Exception {
        mvc.perform(get("/internal/projects/99999999-0000-0000-0000-000000000000").header("X-Service-Token", TOKEN))
                .andExpect(status().isNotFound());
        mvc.perform(get("/internal/users/99999999-0000-0000-0000-000000000000").header("X-Service-Token", TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingTokenIs401() throws Exception {
        mvc.perform(get("/internal/projects/" + PROJECT_1)).andExpect(status().isUnauthorized());
        mvc.perform(get("/internal/users/" + MARCO)).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongTokenIs401() throws Exception {
        mvc.perform(get("/internal/projects/" + PROJECT_1).header("X-Service-Token", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userIdentityDoesNotSubstituteForServiceToken() throws Exception {
        mvc.perform(get("/internal/projects/" + PROJECT_1).header("X-User-Id", MARCO))
                .andExpect(status().isUnauthorized());
    }
}
