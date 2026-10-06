package com.taskify.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Taskify gateway: the single origin for the browser. It routes API calls to the
 * project and task services and is the single place where real authentication will be added later.
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * Starts the gateway.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
