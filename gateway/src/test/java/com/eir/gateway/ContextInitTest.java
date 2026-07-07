package com.eir.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ContextInitTest
{
    private ContextInit contextInit;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private GatewaySecurityContext gatewaySecurityContext;

    @BeforeEach
    void setUp()
    {
        InetSocketAddress socketAddress = mock(InetSocketAddress.class);
        InetAddress inetAddress = mock(InetAddress.class);
        contextInit = new ContextInit();
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        exchange = mock(ServerWebExchange.class);

        when(inetAddress.getHostAddress()).thenReturn("192.168.1.1");
        when(socketAddress.getAddress()).thenReturn(inetAddress);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getRemoteAddress()).thenReturn(socketAddress);
        when(exchange.getRequest().getRemoteAddress()).thenReturn(socketAddress);

        Map<String, Object> attributes = new HashMap<>();
        when(exchange.getAttributes()).thenReturn(attributes);

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_shouldCreataSecurityContextSetIp()
    {
        Mono<Void> result = contextInit.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        GatewaySecurityContext ctx = (GatewaySecurityContext)
                exchange.getAttributes().get("securityContext");


        assertNotNull(ctx);
        assertEquals("192.168.1.1",ctx.getRequestIp());
    }

    @Test
    void filter_shouldCallChainFilter()
    {
        StepVerifier.create(contextInit.filter(exchange,chain)).verifyComplete();

        verify(chain).filter(exchange);
        verify(chain, times(1)).filter(exchange);
        verifyNoMoreInteractions(chain);
    }

    @Test
    void getOrder_shouldReturnNegative3()
    {
        int order = contextInit.getOrder();
        assertEquals(-3, order);
    }
}
