package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.common.jwt.JwtProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class HeaderInjectHandler implements AuthHandler {

    JwtProvider jwtProvider;
    public HeaderInjectHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, GatewayFilterChain chain, HandlerChain next, GatewaySecurityContext context)
    {
        String userId = jwtProvider.getUserIdFromToken(context.getToken());
        context.setUserId(userId);
        return next.next(exchange, chain, context);
    }




}
