package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.gateway.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermitAllHandlerTest
{
    private PermitAllHandler handler;
    private SecurityProperties securityProperties;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private AuthHandler.HandlerChain next;
    private GatewaySecurityContext context;

    @BeforeEach
    void setUp()
    {
        securityProperties = mock(SecurityProperties.class);
        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);
        next = mock(AuthHandler.HandlerChain.class);
        context = new GatewaySecurityContext();

        when(chain.filter(any())).thenReturn(Mono.empty());
        when(next.next(any(), any(), any())).thenReturn(Mono.empty());
        when(securityProperties.getPublicPaths()).thenReturn(List.of("/api/auth"));

        handler = new PermitAllHandler(securityProperties);
    }

    private void mockPath(String path)
    {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        RequestPath requestPath = mock(RequestPath.class);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn(path);
    }

    @Test
    void publicPath_shouldCallChainFilter()
    {
        mockPath("/api/auth/login");

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        verify(chain, times(1)).filter(exchange);
        verify(next, never()).next(any(), any(), any());
    }

    @Test
    void privatePath_shouldCallNext()
    {
        mockPath("/api/patients/list");

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        verify(next, times(1)).next(exchange, chain, context);
        verify(chain, never()).filter(any());
    }
}
