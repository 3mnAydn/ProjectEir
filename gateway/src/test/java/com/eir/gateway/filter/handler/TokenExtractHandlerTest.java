package com.eir.gateway.filter.handler;

import com.eir.gateway.GatewaySecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenExtractHandlerTest
{
    private TokenExtractHandler handler;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private AuthHandler.HandlerChain next;
    private GatewaySecurityContext context;
    private ServerHttpRequest request;
    private HttpHeaders headers;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp()
    {
        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);
        next = mock(AuthHandler.HandlerChain.class);
        context = new GatewaySecurityContext();
        request = mock(ServerHttpRequest.class);
        headers = mock(HttpHeaders.class);
        response = mock(ServerHttpResponse.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getResponse()).thenReturn(response);
        when(next.next(any(), any(), any())).thenReturn(Mono.empty());
        when(response.setComplete()).thenReturn(Mono.empty());

        handler = new TokenExtractHandler();
    }

    @Test
    void validBearerToken_shouldExtractAndProceed()
    {
        when(headers.getFirst("Authorization")).thenReturn("Bearer my.jwt.token");

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        assertEquals("my.jwt.token", context.getToken());
        verify(next, times(1)).next(exchange, chain, context);
        verify(response, never()).setStatusCode(any());
        verify(response, never()).setComplete();
    }

    @Test
    void missingAuthHeader_shouldReturn401()
    {
        when(headers.getFirst("Authorization")).thenReturn(null);

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response, times(1)).setComplete();
        verify(next, never()).next(any(), any(), any());
    }

    @Test
    void invalidAuthHeader_shouldReturn401()
    {
        when(headers.getFirst("Authorization")).thenReturn("Invalid no-bearer");

        Mono<Void> result = handler.handle(exchange, chain, next, context);

        StepVerifier.create(result).verifyComplete();
        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response, times(1)).setComplete();
        verify(next, never()).next(any(), any(), any());
    }
}
