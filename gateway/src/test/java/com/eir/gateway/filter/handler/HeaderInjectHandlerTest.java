package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeaderInjectHandlerTest
{
    private HeaderInjectHandler handler;
    private JwtProvider jwtProvider;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private AuthHandler.HandlerChain next;
    private GatewaySecurityContext context;

    @BeforeEach
    void setUp()
    {
        jwtProvider = mock(JwtProvider.class);
        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);
        next = mock(AuthHandler.HandlerChain.class);
        context = new GatewaySecurityContext();

        context.setToken("my.jwt.token");

        when(next.next(any(), any(), any())).thenReturn(Mono.empty());

        handler = new HeaderInjectHandler(jwtProvider);
    }

    @Test
    void shouldInjectUserIdAndProceed()
    {
        when(jwtProvider.getUserIdFromToken("my.jwt.token")).thenReturn("user-42");

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        assertEquals("user-42", context.getUserId());
        verify(next, times(1)).next(exchange, chain, context);
    }
}
