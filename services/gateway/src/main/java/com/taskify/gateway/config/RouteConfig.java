package com.taskify.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Routing table. Task routes come first because {@code /api/v1/projects/{id}/tasks} belongs to the
 * task service while the other {@code /api/v1/projects} paths belong to the project service.
 * {@code /internal/**} has no route, so it is never reachable through the gateway (404).
 */
@Configuration
public class RouteConfig {

    /**
     * Builds the routes.
     *
     * @param builder           route builder
     * @param projectServiceUrl base URL of project-service
     * @param taskServiceUrl    base URL of task-service
     * @return the route locator
     */
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
            @Value("${taskify.gateway.project-service-url}") String projectServiceUrl,
            @Value("${taskify.gateway.task-service-url}") String taskServiceUrl) {
        return builder.routes()
                .route("tasks-of-project", r -> r.path("/api/v1/projects/*/tasks").uri(taskServiceUrl))
                .route("tasks", r -> r.path("/api/v1/tasks/**").uri(taskServiceUrl))
                .route("users", r -> r.path("/api/v1/users/**").uri(projectServiceUrl))
                .route("projects", r -> r.path("/api/v1/projects", "/api/v1/projects/*").uri(projectServiceUrl))
                .build();
    }
}
