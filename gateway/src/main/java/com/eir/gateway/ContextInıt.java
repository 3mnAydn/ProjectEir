package com.eir.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

class ContextInit implements GlobalFilter, Ordered
{


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        GatewaySecurityContext context = new GatewaySecurityContext();
        context.setRequestIp(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        exchange.getAttributes().put("securityContext", context);
        return chain.filter(exchange);    }

    @Override
    public int getOrder() {
        return -3;
    }
}
