package com.taskify.task.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Contract test for the calls task-service makes to project-service's internal endpoints
 * (see {@code contracts/project-service.openapi.yaml}).
 */
class ProjectServiceClientContractTest {

    static final String TOKEN = "contract-test-token-0123456789abcdef";
    static final UUID PROJECT = UUID.fromString("10000000-0000-0000-0000-000000000001");
    static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    WireMockServer server;
    ProjectServiceClient client;

    @BeforeEach
    void start() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();
        client = new ProjectServiceClient("http://localhost:" + server.port(), TOKEN,
                Duration.ofSeconds(2), Duration.ofSeconds(1));
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void projectExistsAndTokenIsSent() {
        server.stubFor(get(urlEqualTo("/internal/projects/" + PROJECT))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")));
        assertThat(client.projectExists(PROJECT)).isTrue();
        server.verify(getRequestedFor(urlEqualTo("/internal/projects/" + PROJECT))
                .withHeader("X-Service-Token", equalTo(TOKEN)));
    }

    @Test
    void unknownProjectIsFalse() {
        server.stubFor(get(urlEqualTo("/internal/projects/" + PROJECT)).willReturn(aResponse().withStatus(404)));
        assertThat(client.projectExists(PROJECT)).isFalse();
    }

    @Test
    void userExistsAndTokenIsSent() {
        server.stubFor(get(urlEqualTo("/internal/users/" + USER))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")));
        assertThat(client.userExists(USER)).isTrue();
        server.verify(getRequestedFor(urlEqualTo("/internal/users/" + USER))
                .withHeader("X-Service-Token", equalTo(TOKEN)));
    }

    @Test
    void unknownUserIsFalse() {
        server.stubFor(get(urlEqualTo("/internal/users/" + USER)).willReturn(aResponse().withStatus(404)));
        assertThat(client.userExists(USER)).isFalse();
    }

    @Test
    void serverErrorFailsClosed() {
        server.stubFor(get(urlEqualTo("/internal/projects/" + PROJECT)).willReturn(aResponse().withStatus(500)));
        assertThatThrownBy(() -> client.projectExists(PROJECT)).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void rejectedTokenFailsClosed() {
        server.stubFor(get(urlEqualTo("/internal/users/" + USER)).willReturn(aResponse().withStatus(401)));
        assertThatThrownBy(() -> client.userExists(USER)).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void timeoutFailsClosed() {
        server.stubFor(get(urlEqualTo("/internal/users/" + USER))
                .willReturn(aResponse().withStatus(200).withFixedDelay(3000)));
        assertThatThrownBy(() -> client.userExists(USER)).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void unreachableServerFailsClosed() {
        server.stop();
        assertThatThrownBy(() -> client.projectExists(PROJECT)).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void answersAreNotCached() {
        server.stubFor(get(urlEqualTo("/internal/users/" + USER)).willReturn(aResponse().withStatus(200)));
        client.userExists(USER);
        client.userExists(USER);
        server.verify(2, getRequestedFor(urlEqualTo("/internal/users/" + USER)));
    }
}
