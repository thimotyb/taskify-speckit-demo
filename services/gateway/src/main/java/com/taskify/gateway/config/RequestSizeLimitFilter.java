package com.taskify.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Rejects requests whose declared body size exceeds the configured limit (413), before they reach a
 * service. Requests that stream a body without a length are rejected too (411), because no payload
 * in Taskify is large.
 */
@Component
public class RequestSizeLimitFilter implements GlobalFilter, Ordered {

    private final long maxBytes;

    /**
     * Creates the filter.
     *
     * @param maxBytes maximum accepted request body size in bytes
     */
    public RequestSizeLimitFilter(@Value("${taskify.gateway.max-request-bytes:65536}") long maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long length = exchange.getRequest().getHeaders().getContentLength();
        boolean hasBody = exchange.getRequest().getHeaders().containsKey("Transfer-Encoding");
        if (length > maxBytes) {
            return ProblemResponses.write(exchange, HttpStatus.PAYLOAD_TOO_LARGE, "payload-too-large",
                    "Request too large", "The request is too large.");
        }
        if (hasBody && length < 0) {
            return ProblemResponses.write(exchange, HttpStatus.LENGTH_REQUIRED, "length-required",
                    "Length required", "The request must declare its size.");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
