package com.taskify.gateway.config;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Limits write requests (POST, PUT, PATCH) per acting user per minute (default 60) and answers 429
 * when the limit is exceeded. The key is the {@code X-User-Id} header when it is a valid UUID,
 * otherwise the client address, and the number of tracked keys is capped so the table cannot be grown
 * without bound.
 */
@Component
public class WriteRateLimitFilter implements GlobalFilter, Ordered {

    private static final Set<HttpMethod> WRITES = Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);
    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_KEYS = 10_000;

    private final int limitPerMinute;
    private final Clock clock;
    private final ConcurrentHashMap<String, long[]> windows = new ConcurrentHashMap<>();

    /**
     * Creates the filter.
     *
     * @param limitPerMinute maximum write requests per key per minute
     */
    @org.springframework.beans.factory.annotation.Autowired
    public WriteRateLimitFilter(@Value("${taskify.gateway.write-rate-limit-per-minute:60}") int limitPerMinute) {
        this(limitPerMinute, Clock.systemUTC());
    }

    /**
     * Creates the filter with an explicit clock (for tests).
     *
     * @param limitPerMinute maximum write requests per key per minute
     * @param clock          time source
     */
    WriteRateLimitFilter(int limitPerMinute, Clock clock) {
        this.limitPerMinute = limitPerMinute;
        this.clock = clock;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!WRITES.contains(exchange.getRequest().getMethod()) || allow(keyOf(exchange))) {
            return chain.filter(exchange);
        }
        return ProblemResponses.write(exchange, HttpStatus.TOO_MANY_REQUESTS, "rate-limited", "Too many requests",
                "Too many changes in a short time. Please wait a moment and try again.");
    }

    private boolean allow(String key) {
        long now = clock.millis();
        if (windows.size() > MAX_KEYS) {
            windows.values().removeIf(w -> now - w[0] >= WINDOW_MILLIS);
        }
        String effective = windows.size() > MAX_KEYS && !windows.containsKey(key) ? "overflow" : key;
        long[] window = windows.computeIfAbsent(effective, k -> new long[] {now, 0});
        synchronized (window) {
            if (now - window[0] >= WINDOW_MILLIS) {
                window[0] = now;
                window[1] = 0;
            }
            window[1]++;
            return window[1] <= limitPerMinute;
        }
    }

    private static String keyOf(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (header != null) {
            try {
                return "user:" + UUID.fromString(header.trim());
            } catch (IllegalArgumentException ignored) {
                // fall through to the client address
            }
        }
        var address = exchange.getRequest().getRemoteAddress();
        return "ip:" + (address == null ? "unknown" : address.getAddress().getHostAddress());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
