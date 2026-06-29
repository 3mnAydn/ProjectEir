package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface AuthHandler {
    Mono<Void> handle(ServerWebExchange exchange, GatewayFilterChain chain,
                      HandlerChain next, GatewaySecurityContext context);

    @FunctionalInterface
    interface HandlerChain {
        Mono<Void> next(ServerWebExchange exchange, GatewayFilterChain chain,
                        GatewaySecurityContext context);
    }
}
