package com.eir.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ContextInitTest
{
    private ContextInit contextInit;
    private ServerWebExchange serverWebExchange;
    private GatewayFilterChain gatewayFilterChain;
    private GatewaySecurityContext gatewaySecurityContext;

    @BeforeEach
    void setUp()
    {
        InetSocketAddress inetSocketAddress = mock(InetSocketAddress.class);
        InetAddress inetAddress = mock(InetAddress.class);
        when(inetAddress.getHostAddress()).thenReturn("192.168.1.1");
        when(socketAddress.getAddress()).thenReturn(inetAddress);

        exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(mock(ServerHttpRequest.class));
        when(exchange.getRequest().getRemoteAddress()).thenReturn(socketAddress);

        Map<String, Object> attributes = new HashMap<>();
        when(exchange.getAttributes()).thenReturn(attributes);

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }
}
