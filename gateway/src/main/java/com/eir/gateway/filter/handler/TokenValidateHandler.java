package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.gateway.security.TokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenValidateHandler implements AuthHandler {

    TokenProvider tokenProvider;
    public TokenValidateHandler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, GatewayFilterChain chain, HandlerChain next,GatewaySecurityContext context)
    {
        boolean tokenIsValid = tokenProvider.validateToken(context.getToken());
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
