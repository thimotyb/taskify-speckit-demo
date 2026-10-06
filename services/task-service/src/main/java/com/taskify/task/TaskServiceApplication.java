package com.taskify.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Taskify task service, which owns tasks and their comments.
 */
@SpringBootApplication
public class TaskServiceApplication {

    /**
     * Starts the service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
