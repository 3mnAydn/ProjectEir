package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.gateway.security.TokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class HeaderInjectHandler implements AuthHandler {

    TokenProvider tokenProvider;
    public HeaderInjectHandler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, GatewayFilterChain chain, HandlerChain next, GatewaySecurityContext context)
    {
        String userId = tokenProvider.getUserIdFromToken(context.getToken());
        context.setUserId(userId);
        return next.next(exchange, chain, context);
    }




}
