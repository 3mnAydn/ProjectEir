package com.eir.gateway.filter;

import com.eir.gateway.GatewaySecurityContext;
import com.eir.gateway.filter.handler.AuthHandler;
import com.eir.gateway.filter.handler.HeaderInjectHandler;
import com.eir.gateway.filter.handler.PermitAllHandler;
import com.eir.gateway.filter.handler.TokenExtractHandler;
import com.eir.gateway.filter.handler.TokenValidateHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest
{
    private JwtAuthenticationFilter filter;
    private PermitAllHandler permitAllHandler;
    private TokenExtractHandler tokenExtractHandler;
    private TokenValidateHandler tokenValidateHandler;
    private HeaderInjectHandler headerInjectHandler;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp()
    {
        permitAllHandler = mock(PermitAllHandler.class);
        tokenExtractHandler = mock(TokenExtractHandler.class);
        tokenValidateHandler = mock(TokenValidateHandler.class);
        headerInjectHandler = mock(HeaderInjectHandler.class);
        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);
        attributes = new HashMap<>();
        attributes.put("securityContext", new GatewaySecurityContext());

        when(exchange.getAttributes()).thenReturn(attributes);

        filter = new JwtAuthenticationFilter(
                permitAllHandler, tokenExtractHandler,
                tokenValidateHandler, headerInjectHandler
        );
    }

    @Test
    void getOrder_shouldReturnNegative1()
    {
        assertEquals(-1, filter.getOrder());
    }

    @Test
    void filter_shouldCallPermitAllHandler()
    {
        when(permitAllHandler.handle(any(), any(), any(), any())).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(permitAllHandler, times(1)).handle(eq(exchange), eq(chain), any(), any());
    }
}
