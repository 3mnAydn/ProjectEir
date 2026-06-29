package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.gateway.config.SecurityProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class PermitAllHandler implements AuthHandler
{
    private final SecurityProperties securityProperties;


    public PermitAllHandler(SecurityProperties securityProperties)
    {
        this.securityProperties = securityProperties;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, GatewayFilterChain chain, HandlerChain next, GatewaySecurityContext context)
    {

        if(isPathPublic(exchange))
            return chain.filter(exchange);

        return next.next(exchange,chain,context);

    }

    private boolean isPathPublic(ServerWebExchange exchange)
    {
        String path = exchange.getRequest().getPath().value();
        return securityProperties.getPublicPaths().stream().anyMatch(path::startsWith);
    }


}
