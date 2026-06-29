package com.eir.gateway.filter;

import com.eir.gateway.GatewaySecurityContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final int MAX_REQUESTS = 100;
    private final int TIME_WINDOW = 60;

    public RateLimitFilter(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        GatewaySecurityContext context = (GatewaySecurityContext) exchange.getAttributes().get("securityContext");
        long currentMinute = Instant.now().getEpochSecond() / TIME_WINDOW;
        String key = "ratelimit:" + context.getRequestIp() + ":" + currentMinute;

        return reactiveRedisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return reactiveRedisTemplate.expire(key, Duration.ofSeconds(TIME_WINDOW))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > MAX_REQUESTS) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() { return -2; }
}