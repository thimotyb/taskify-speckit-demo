package com.taskify.task.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client for project-service's internal endpoints, used to check that users and projects exist.
 *
 * <p>Nothing is cached: every check goes to project-service, so an outage always fails closed.
 * Unreachable, timed-out, or unexpected answers raise {@link UpstreamUnavailableException}.
 */
@Component
public class ProjectServiceClient {

    private final RestClient client;
    private final String token;

    /**
     * Creates the client.
     *
     * @param baseUrl        project-service base URL
     * @param token          shared service token sent as {@code X-Service-Token}
     * @param connectTimeout connect timeout
     * @param readTimeout    read timeout
     */
    public ProjectServiceClient(@Value("${taskify.project-service.base-url}") String baseUrl,
            @Value("${taskify.service-token:}") String token,
            @Value("${taskify.project-service.connect-timeout:2s}") Duration connectTimeout,
            @Value("${taskify.project-service.read-timeout:2s}") Duration readTimeout) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(connectTimeout).build());
        factory.setReadTimeout(readTimeout);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.token = token;
    }

    /**
     * Checks whether a user exists.
     *
     * @param userId user id
     * @return true if project-service knows the user
     * @throws UpstreamUnavailableException if project-service cannot answer
     */
    public boolean userExists(UUID userId) {
        return exists("/internal/users/{id}", userId);
    }

    /**
     * Checks whether a project exists.
     *
     * @param projectId project id
     * @return true if project-service knows the project
     * @throws UpstreamUnavailableException if project-service cannot answer
     */
    public boolean projectExists(UUID projectId) {
        return exists("/internal/projects/{id}", projectId);
    }

    private boolean exists(String path, UUID id) {
        try {
            HttpStatusCode status = client.get().uri(path, id)
                    .header("X-Service-Token", token)
                    .exchange((request, response) -> response.getStatusCode());
            if (status.is2xxSuccessful()) {
                return true;
            }
            if (status.value() == 404) {
                return false;
            }
            throw new UpstreamUnavailableException("Unexpected status " + status.value() + " from project-service",
                    null);
        } catch (UpstreamUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new UpstreamUnavailableException("project-service unreachable", e);
        }
    }
}
