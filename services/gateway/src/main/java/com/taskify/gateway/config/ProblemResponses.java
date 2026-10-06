package com.taskify.gateway.config;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Writes small RFC 9457 problem responses from gateway filters.
 */
final class ProblemResponses {

    private ProblemResponses() {
    }

    /**
     * Completes the exchange with a problem response.
     *
     * @param exchange the exchange
     * @param status   HTTP status
     * @param code     machine-readable problem code
     * @param title    short title
     * @param detail   safe, non-technical detail
     * @return completion signal
     */
    static Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String code, String title, String detail) {
        String body = "{\"type\":\"urn:taskify:problem:" + code + "\",\"title\":\"" + title + "\",\"status\":"
                + status.value() + ",\"detail\":\"" + detail + "\"}";
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
