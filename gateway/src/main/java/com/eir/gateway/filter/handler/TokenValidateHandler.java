package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.common.jwt.JwtProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenValidateHandler implements AuthHandler {

    JwtProvider jwtProvider;
    public TokenValidateHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, GatewayFilterChain chain, HandlerChain next,GatewaySecurityContext context)
    {
        boolean tokenIsValid = jwtProvider.validateToken(context.getToken());
        if(tokenIsValid)
        {
            return next.next(exchange, chain, context);
        }
        else
        {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }


}
