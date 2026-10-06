package com.taskify.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Taskify project service, which owns the predefined users and the projects.
 */
@SpringBootApplication
public class ProjectServiceApplication {

    /**
     * Starts the service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}
