package com.eir.gateway.filter.handler;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.eir.gateway.GatewaySecurityContext;

@Component
public class TokenExtractHandler implements AuthHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, GatewayFilterChain chain, HandlerChain next, GatewaySecurityContext context)
    {
        String token = extractToken(exchange);
        if(token != null)
        {
            context.setToken(token);
            return next.next(exchange, chain, context);

        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private String extractToken(ServerWebExchange exchange)
    {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer "))
        {
            return header.substring(7);
        }
        return null;
    }
}
