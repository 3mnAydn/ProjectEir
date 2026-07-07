package com.eir.gateway.filter;

import com.eir.gateway.filter.handler.*;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.eir.gateway.GatewaySecurityContext;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {


    private final AuthHandler permitAllHandler;
    private final AuthHandler headerInjectHandler;
    private final AuthHandler tokenValidateHandler;
    private final AuthHandler tokenExtractHandler;

    public JwtAuthenticationFilter(
            PermitAllHandler permitAllHandler,
            TokenExtractHandler tokenExtractHandler,
            TokenValidateHandler tokenValidateHandler,
            HeaderInjectHandler headerInjectHandler) {
        this.permitAllHandler = permitAllHandler;
        this.tokenExtractHandler = tokenExtractHandler;
        this.tokenValidateHandler = tokenValidateHandler;
        this.headerInjectHandler = headerInjectHandler;

    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {

        GatewaySecurityContext context = (GatewaySecurityContext) exchange.getAttributes().get("securityContext");
        AuthHandler.HandlerChain chain3 = (ex, ch, ctx) ->
                headerInjectHandler.handle(ex, ch, (e, c, c2) -> c.filter(e), ctx);
        AuthHandler.HandlerChain chain2 = (ex, ch, ctx) ->
                tokenValidateHandler.handle(ex, ch, chain3, ctx);
        AuthHandler.HandlerChain chain1 = (ex, ch, ctx) ->
                tokenExtractHandler.handle(ex, ch, chain2, ctx);
        return permitAllHandler.handle(exchange, chain, chain1, context);
    }

    @Override
    public int getOrder()
    {
        return -1;
    }
}
