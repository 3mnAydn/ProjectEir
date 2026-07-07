package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenValidateHandlerTest
{
    private TokenValidateHandler handler;
    private JwtProvider jwtProvider;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private AuthHandler.HandlerChain next;
    private GatewaySecurityContext context;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp()
    {
        jwtProvider = mock(JwtProvider.class);
        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);
        next = mock(AuthHandler.HandlerChain.class);
        context = new GatewaySecurityContext();
        response = mock(ServerHttpResponse.class);

        context.setToken("my.jwt.token");

        when(exchange.getResponse()).thenReturn(response);
        when(next.next(any(), any(), any())).thenReturn(Mono.empty());
        when(response.setComplete()).thenReturn(Mono.empty());

        handler = new TokenValidateHandler(jwtProvider);
    }

    @Test
    void validToken_shouldProceed()
    {
        when(jwtProvider.validateToken("my.jwt.token")).thenReturn(true);

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        verify(next, times(1)).next(exchange, chain, context);
        verify(response, never()).setStatusCode(any());
        verify(response, never()).setComplete();
    }

    @Test
    void invalidToken_shouldReturn401()
    {
        when(jwtProvider.validateToken("my.jwt.token")).thenReturn(false);

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response, times(1)).setComplete();
        verify(next, never()).next(any(), any(), any());
    }
}
